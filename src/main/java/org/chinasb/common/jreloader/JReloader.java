package org.chinasb.common.jreloader;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.chinasb.common.jreloader.compiler.Compiler;
import org.chinasb.common.jreloader.compiler.support.JdkCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * JReloader.
 * 
 * @author zhujuan
 *
 */
public class JReloader {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JReloader.class);

	private final Compiler complier;
	private final ConcurrentMap<Integer, Reloader> reloaders;
	private final FileAlterationMonitor monitor;

	public JReloader() throws Exception {
	    complier = new JdkCompiler();
		reloaders = new ConcurrentHashMap<Integer, Reloader>();
		monitor = new FileAlterationMonitor(Integer.getInteger(
				"jreloader.interval", 5000));
		monitor.start();
		try {
            File folder = new File(System.getProperty("jreloader.dirs", "."));
            if (!folder.exists()) {
                LOGGER.warn("监控目录[{}]不存在!", folder);
                try {
                    folder.mkdirs();
                    LOGGER.info("自动创建监控目录[{}]成功!", folder);
                } catch (Exception e) {
                    LOGGER.error("自动创建监控目录[{}]失败!", folder);
                }
            }
			FileAlterationObserver observer = new FileAlterationObserver(
					folder, new FileFilter() {

						@Override
						public boolean accept(File pathname) {
							return pathname.isDirectory()
									|| pathname.getName().endsWith(".java");
						}

					});

			observer.addListener(new FileAlterationListenerAdaptor() {

				@Override
				public void onFileChange(File file) {
					reload(file);
				}

				@Override
				public void onFileCreate(File file) {
					reload(file);
				}
			});
			observer.initialize();
			monitor.addObserver(observer);
			LOGGER.info("重载脚本功能启动成功，开始监控目录[{}]...", folder);
		} catch (Exception e) {
			LOGGER.error("重载脚本功能启动失败!", e);
		}
	}

    public void stop() {
        if (monitor != null) {
            try {
                monitor.stop();
            } catch (Exception e) {
                LOGGER.error("重载脚本功能停止失败!", e);
            }
        }
    }
	
	private void reload(File file) {
		try {
			Class<?> clazz = complier.compile(FileUtils.readFileToString(file));
			Reloadable reloadable = clazz.getAnnotation(Reloadable.class);
			if (reloadable != null) {
				Reloader reloader = reloaders.get(reloadable.module());
				if (reloader == null) {
					LOGGER.error("没有找到功能模块[{}], 脚本[{}]重载失败!",
							reloadable.module(), clazz.getName());
					return;
				}
				reloader.reload(clazz);
			}
		} catch (Throwable e) {
			FormattingTuple message = MessageFormatter.format("脚本[{}]编译失败!",
					file);
			LOGGER.error(message.getMessage(), e);
		}
	}
	
	public boolean addReloader(int moudle, Reloader reloader) {
		if (reloaders.containsKey(moudle)) {
			return false;
		}
		return reloaders.putIfAbsent(moudle, reloader) == null ? true : false;
	}

	public static interface Reloader {
		public void reload(Class<?> clazz);
	}

	public static abstract class BaseReloader implements Reloader {

		public abstract void onReload(Class<?> clazz) throws Throwable;

		public abstract int getMoudle();

		@Override
		public void reload(Class<?> clazz) {
			try {
				onReload(clazz);
				LOGGER.info("模块[{}], 脚本 [{}]完成重载 ...", getMoudle(),
						clazz.getName());
			} catch (Throwable e) {
				FormattingTuple message = MessageFormatter.format(
						"模块[{}], 脚本[{}]重载失败!", getMoudle(), clazz.getName());
				LOGGER.error(message.getMessage(), e);
			}
		}
	}
}
