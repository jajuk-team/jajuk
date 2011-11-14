/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision$
 */
package org.jajuk.events;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jajuk.JajukTestCase;
import org.jajuk.ThreadTestHelper;

/**
 * DOCUMENT_ME.
 */
public class TestObserverRegistry extends JajukTestCase {

  /** The Constant NUMBER_OF_THREADS.  DOCUMENT_ME */
  private static final int NUMBER_OF_THREADS = 15; // 15 is the limit on
  // concurrent events
  /** The Constant NUMBER_OF_TESTS.  DOCUMENT_ME */
  private static final int NUMBER_OF_TESTS = 1000;

  /** DOCUMENT_ME. */
  private AtomicInteger called = new AtomicInteger(0);

  /**
   * Test method for.
   *
   * {@link org.jajuk.events.ObserverRegistry#notifySync(org.jajuk.events.JajukEvent)}
   * .
   */
  public void testNotifySync() {
    ObserverRegistry registry = new ObserverRegistry();
    registry.notifySync(new JajukEvent(JajukEvents.PLAYER_PLAY));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.events.ObserverRegistry#register(org.jajuk.events.JajukEvents, org.jajuk.events.Observer)}
   * .
   */
  public void testRegister() {
    ObserverRegistry registry = new ObserverRegistry();
    registry.register(JajukEvents.PLAYER_PLAY, new LocalObserver(called));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.events.ObserverRegistry#unregister(org.jajuk.events.JajukEvents, org.jajuk.events.Observer)}
   * .
   */
  public void testUnregister() {
    ObserverRegistry registry = new ObserverRegistry();
    registry.unregister(JajukEvents.PLAYER_PLAY, new LocalObserver(called));
  }

  /**
   * Test below zero.
   * DOCUMENT_ME
   */
  public void testBelowZero() {
    ObserverRegistry registry = new ObserverRegistry();
    Observer observer = new LocalObserver(called);

    // first register
    registry.register(JajukEvents.FILE_FINISHED, observer);

    assertEquals(0, called.get());

    // then notifySync
    registry.notifySync(new JajukEvent(JajukEvents.FILE_FINISHED));

    assertEquals(1, called.get());

    // then unregister again
    registry.unregister(JajukEvents.FILE_FINISHED, observer);
  }

  /**
   * Test exception.
   * DOCUMENT_ME
   */
  public void testException() {
    ObserverRegistry registry = new ObserverRegistry();
    Observer observer = new LocalObserver(true, called);

    // first register
    registry.register(JajukEvents.FILE_FINISHED, observer);

    assertEquals(0, called.get());

    // then notifySync, this will not return an error even if an exception
    // occurred
    registry.notifySync(new JajukEvent(JajukEvents.FILE_FINISHED));

    assertEquals(1, called.get());

    // then unregister again
    registry.unregister(JajukEvents.FILE_FINISHED, observer);
  }

  /**
   * Test multiple threads.
   * DOCUMENT_ME
   *
   * @throws Exception the exception
   */
  public void testMultipleThreads() throws Exception {
    final ObserverRegistry registry = new ObserverRegistry();
    Observer observer = new LocalObserver(called);

    // first register
    registry.register(JajukEvents.FILE_FINISHED, observer);

    assertEquals(0, called.get());

    ThreadTestHelper helper = new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

    helper.executeTest(new ThreadTestHelper.TestRunnable() {
      @Override
      public void doEnd(int threadnum) throws Exception {
        // nothing to do
      }

      @Override
      public void run(int threadnum, int iter) {
        // then notifySync
        registry.notifySync(new JajukEvent(JajukEvents.FILE_FINISHED));
      }
    });

    assertEquals(NUMBER_OF_THREADS * NUMBER_OF_TESTS, called.get());

    // then unregister again
    registry.unregister(JajukEvents.FILE_FINISHED, observer);
  }

  /**
   * Test multiple threads wait.
   * DOCUMENT_ME
   *
   * @throws Exception the exception
   */
  public void testMultipleThreadsWait() throws Exception {
    final ObserverRegistry registry = new ObserverRegistry();

    // set 100ms wait time to reach event queue size on normal speed machines
    Observer observer = new LocalObserver(100, called);

    // first register
    registry.register(JajukEvents.FILE_FINISHED, observer);

    assertEquals(0, called.get());

    // more threads so that we reach the limit of 15 concurrent events
    // a bit fewer tests as they will need some time
    ThreadTestHelper helper = new ThreadTestHelper(NUMBER_OF_THREADS * 2, NUMBER_OF_TESTS / 20);

    helper.executeTest(new ThreadTestHelper.TestRunnable() {
      @Override
      public void doEnd(int threadnum) throws Exception {
        // nothing to do
      }

      @Override
      public void run(int threadnum, int iter) {
        // then notifySync
        registry.notifySync(new JajukEvent(JajukEvents.FILE_FINISHED));
      }
    });

    // can not test this as we have overflows here!
    // assertEquals(NUMBER_OF_THREADS * NUMBER_OF_TESTS, called.get());

    // then unregister again
    registry.unregister(JajukEvents.FILE_FINISHED, observer);
  }

  /**
   * Test multiple threads multiple observers.
   * DOCUMENT_ME
   *
   * @throws Exception the exception
   */
  public void testMultipleThreadsMultipleObservers() throws Exception {
    final ObserverRegistry registry = new ObserverRegistry();
    Observer observer1 = new LocalObserver(called);
    Observer observer2 = new LocalObserver(called);

    // first register
    registry.register(JajukEvents.FILE_FINISHED, observer1);
    registry.register(JajukEvents.FILE_FINISHED, observer2);

    assertEquals(0, called.get());

    ThreadTestHelper helper = new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

    helper.executeTest(new ThreadTestHelper.TestRunnable() {
      @Override
      public void doEnd(int threadnum) throws Exception {
        // nothing to do
      }

      @Override
      public void run(int threadnum, int iter) {
        // then notifySync
        registry.notifySync(new JajukEvent(JajukEvents.FILE_FINISHED));
      }
    });

    // now we were called twice as many times because of two observers...
    assertEquals(2 * NUMBER_OF_THREADS * NUMBER_OF_TESTS, called.get());

    // then unregister again
    registry.unregister(JajukEvents.FILE_FINISHED, observer2);
    registry.unregister(JajukEvents.FILE_FINISHED, observer1);
  }

  /**
   * Test high priority observer.
   * DOCUMENT_ME
   *
   * @throws Exception the exception
   */
  public void testHighPriorityObserver() throws Exception {
    final ObserverRegistry registry = new ObserverRegistry();
    LocalObserver observer1 = new LocalObserver(called);
    Observer observer2 = new LocalHighPriorityObserver(observer1, called);

    // first register
    registry.register(JajukEvents.FILE_FINISHED, observer1);
    registry.register(JajukEvents.FILE_FINISHED, observer2);

    assertEquals(0, called.get());

    ThreadTestHelper helper = new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

    helper.executeTest(new ThreadTestHelper.TestRunnable() {
      @Override
      public void doEnd(int threadnum) throws Exception {
        // nothing to do
      }

      @Override
      public void run(int threadnum, int iter) {
        // then notifySync
        registry.notifySync(new JajukEvent(JajukEvents.FILE_FINISHED));
      }
    });

    // now we were called twice as many times because of two observers...
    assertEquals(2 * NUMBER_OF_THREADS * NUMBER_OF_TESTS, called.get());

    // then unregister again
    registry.unregister(JajukEvents.FILE_FINISHED, observer2);
    registry.unregister(JajukEvents.FILE_FINISHED, observer1);
  }

  /**
   * DOCUMENT_ME.
   */
  static class LocalObserver implements Observer {

    /** DOCUMENT_ME. */
    boolean invoked = false;

    /** DOCUMENT_ME. */
    int wait = 0;

    /** DOCUMENT_ME. */
    boolean exception = false;

    /** DOCUMENT_ME. */
    AtomicInteger called;

    /**
     * Instantiates a new local observer.
     *
     * @param called DOCUMENT_ME
     */
    public LocalObserver(AtomicInteger called) {
      super();
      this.called = called;
    }

    /**
     * Instantiates a new local observer.
     *
     * @param wait DOCUMENT_ME
     * @param called DOCUMENT_ME
     */
    public LocalObserver(int wait, AtomicInteger called) {
      super();
      this.wait = wait;
      this.called = called;
    }

    /**
     * Instantiates a new local observer.
     *
     * @param exception DOCUMENT_ME
     * @param called DOCUMENT_ME
     */
    public LocalObserver(boolean exception, AtomicInteger called) {
      super();
      this.exception = exception;
      this.called = called;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.events.Observer#getRegistrationKeys()
     */
    @Override
    public Set<JajukEvents> getRegistrationKeys() {
      // only used in ObservationManager, not used in this testcase
      Set<JajukEvents> set = new HashSet<JajukEvents>();
      set.add(JajukEvents.ALBUM_CHANGED);
      set.add(JajukEvents.PLAY_ERROR);
      return set;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
     */
    @Override
    public void update(JajukEvent event) {
      called.incrementAndGet();

      if (exception) {
        throw new RuntimeException("Exception requested in update...");
      }

      if (wait > 0) {
        try {
          Thread.sleep(wait);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  /**
   * DOCUMENT_ME.
   */
  private class LocalHighPriorityObserver extends LocalObserver implements HighPriorityObserver {

    /** DOCUMENT_ME. */
    LocalObserver lowprioobserver; // to check if other was not yet called

    /**
     * Instantiates a new local high priority observer.
     *
     * @param lowprioobserver DOCUMENT_ME
     * @param called DOCUMENT_ME
     */
    public LocalHighPriorityObserver(LocalObserver lowprioobserver, AtomicInteger called) {
      super(called);
      this.lowprioobserver = lowprioobserver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jajuk.events.TestObserverRegistry.LocalObserver#update(org.jajuk.
     * events.JajukEvent)
     */
    @Override
    public void update(JajukEvent event) {
      if (lowprioobserver.invoked) {
        throw new RuntimeException("LocalObserver was called before HighPriorityObserver!");
      }
      super.update(event);
    }

  }
}
