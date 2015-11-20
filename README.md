# Getting started
```Java
/**
 * You need only to add a couple of vm arguments, as in the example below:
 * -Djreloader.dirs={java_source_dir} -Djreloader.interval={ms}
 *
 * @Reloadable to be used in class declarations, that can be there to make a class reloadable
 * e.g. 
 * @Reloadable(module={Id})
 * public class xx {}
 */
static final int MOUDLE_COMMAND = 1;

JReloader jReloader = new JReloader();

jReloader.addReloader(MOUDLE_COMMAND, new BaseReloader() {

	@Override
	public void onReload(Class<?> clazz) throws Throwable {
		// doSomething
	}

	@Override
	public int getMoudle() {
		return MOUDLE_COMMAND;
	}

};

// stop
jReloader.stop();
```
