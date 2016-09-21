package app;

public abstract class ManagedThreadRunner extends Thread {
	protected Runnable m_runnable;
	
	protected ManagedThreadRunner(Runnable runnable, String stThreadName) {
		m_runnable = runnable;
		setName(stThreadName);
	}
	
	public void run() {
		Log.logThread("Running thread " + getName());
		m_runnable.run();
		Log.logThread("Done running thread " + getName());
		onThreadFinish();
	}
	
	public abstract void onThreadFinish();
}
