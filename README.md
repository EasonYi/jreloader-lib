# Getting started
```Java
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
```
