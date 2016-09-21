package app;

public abstract class ThreadGroupManager {
	private static final int DEFAULT_THREAD_COUNT = 1;

	private int m_nMaxThreads;
	private int m_nCurrentThreads;
	private Object m_objLock = new Object();

	protected ThreadGroupManager() {
		m_nMaxThreads = DEFAULT_THREAD_COUNT;
		m_nCurrentThreads = 0;
	}

	protected void setMaxThreadCount(int nThreads) {
		if (nThreads >= 1)
			m_nMaxThreads = nThreads;
		Log.logThread("Max thread count = " + m_nMaxThreads);
	}

	protected void submitThread(Thread thread) {
		synchronized (m_objLock) {
			while (!startThread(thread))
				try {
					Log.logThread("Thread " + thread.getName() + " is waiting.");
					m_objLock.wait();
				} catch (InterruptedException e) {
					Log.logThread(e);
				}
		}
	}

	protected boolean startThread(Thread thread) {
		if (m_nCurrentThreads < m_nMaxThreads) {
			m_nCurrentThreads++;
			Log.logThread("Executing thread " + thread.getName());
			thread.start();
			return true;
		}

		return false;
	}

	protected void threadFinished(Thread thread) {
		synchronized (m_objLock) {
			m_nCurrentThreads--;
			Log.logThread("Thread " + thread.getName() + " has finished");
			m_objLock.notify();
		}
	}

	protected boolean allThreadsFinished(boolean bBlockUntilAllFinished) {
		if (!bBlockUntilAllFinished)
			return m_nCurrentThreads == 0;

		synchronized (m_objLock) {
			while (m_nCurrentThreads > 0) {
				try {
					Log.logThread("Waiting for all threads to finish.");
					m_objLock.wait();
				} catch (InterruptedException e) {
					Log.logThread(e);
				}
			}
		}

		Log.logThread("All threads have finished.");
		return true;
	}
}
