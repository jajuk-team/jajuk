/*    
 *  ProccessLauncher is a tool to launch an extern application
 *  in a Java program with stream managed in separate threads.
 * 
 *  Copyright (C) 2006  Fabio MARAZZATO, Yann D'ISANTO
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ext;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jajuk.util.log.Log;

/**
 * http://ydisanto.ftp-developpez.com/tutoriels/j2se/runtime/fichiers/ProcessLauncher.java
 * ProcessLauncher permet de lancer une application externe en consommant les
 * divers fluxs dans des threads separes.
 * 
 * @author Fabio MARAZZATO
 * @author Yann D'ISANTO
 */
public class ProcessLauncher {

  /** DOCUMENT_ME. */
  private OutputStream out = null;

  /** DOCUMENT_ME. */
  private OutputStream err = null;

  /** DOCUMENT_ME. */
  private InputStream in = null;

  /** DOCUMENT_ME. */
  private Process process;

  /** DOCUMENT_ME. */
  private long timeout = 0L;

  /** DOCUMENT_ME. */
  private boolean finished = false;

  /**
   * Instantiates a new process launcher.
   */
  public ProcessLauncher() {
    this(null, null, null, 0L);
  }

  /**
   * The Constructor.
   * 
   * @param out Outputstream vers lequel sera redirige la sortie standard de
   * l'application externe (null pour ne pas rediriger).
   * @param err Outputstream vers lequel sera redirige la sortie d'erreur de
   * l'application externe (null pour ne pas rediriger).
   */
  public ProcessLauncher(OutputStream out, OutputStream err) {
    this(out, err, null, 0L);
  }

  /**
   * The Constructor.
   * 
   * @param out Outputstream vers lequel sera redirige la sortie standard de
   * l'application externe (null pour ne pas rediriger).
   * @param err Outputstream vers lequel sera redirige la sortie d'erreur de
   * l'application externe (null pour ne pas rediriger).
   * @param in InputStream vers lequel sera redirige l'entree standard de
   * l'application externe (null pour ne pas rediriger).
   */
  public ProcessLauncher(OutputStream out, OutputStream err, InputStream in) {
    this(out, err, in, 0L);
  }

  /**
   * The Constructor.
   * 
   * @param out Outputstream vers lequel sera redirige la sortie standard de
   * l'application externe (null pour ne pas rediriger).
   * @param err Outputstream vers lequel sera redirige la sortie d'erreur de
   * l'application externe (null pour ne pas rediriger).
   * @param timeout temps en millisecondes avant de forcer l'arret de l'application
   * externe (0 pour ne jamais forcer l'arret).
   */
  public ProcessLauncher(OutputStream out, OutputStream err, long timeout) {
    this(out, err, null, timeout);
  }

  /**
   * The Constructor.
   * 
   * @param out Outputstream vers lequel sera redirige la sortie standard de
   * l'application externe (null pour ne pas rediriger).
   * @param err Outputstream vers lequel sera redirige la sortie d'erreur de
   * l'application externe (null pour ne pas rediriger).
   * @param in InputStream vers lequel sera redirige l'entree standard de
   * l'application externe (null pour ne pas rediriger).
   * @param timeout temps en millisecondes avant de forcer l'arret de l'application
   * externe (0 pour ne jamais forcer l'arret).
   */
  public ProcessLauncher(OutputStream out, OutputStream err, InputStream in, long timeout) {
    this.out = out;
    this.err = err;
    this.in = in;
    this.timeout = timeout < 0 ? 0L : timeout;
  }

  /**
   * Execute une ligne de commande dans un processus separe.
   * 
   * @param command ligne de commande a executer
   * 
   * @return valeur de retour du processus
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  final public int exec(String command) throws IOException {
    process = Runtime.getRuntime().exec(command);
    return execute();
  }

  /**
   * Execute une commande avec ses parametres dans un processus separe.
   * 
   * @param cmdarray tableau de String contenant la commande et ses parametres
   * 
   * @return valeur de retour du processus
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  final public int exec(String[] cmdarray) throws IOException {
    process = Runtime.getRuntime().exec(cmdarray);
    return execute();
  }

  /**
   * Execute une commande avec ses parametres dans un processus separe en
   * specifiant des variables d'environnement.
   * 
   * @param cmdarray tableau de String contenant la commande et ses parametres
   * @param envp variables d'environnement
   * 
   * @return valeur de retour du processus
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  final public int exec(String[] cmdarray, String[] envp) throws IOException {
    process = Runtime.getRuntime().exec(cmdarray, envp);
    return execute();
  }

  /**
   * Execute une commande avec ses parametres dans un processus separe en
   * specifiant des variables d'environnement et le repertoire de travail.
   * 
   * @param cmdarray tableau de String contenant la commande et ses parametres
   * @param envp variables d'environnement
   * @param dir repertoire de travail
   * 
   * @return valeur de retour du processus
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  final public int exec(String[] cmdarray, String[] envp, File dir) throws IOException {
    process = Runtime.getRuntime().exec(cmdarray, envp, dir);
    return execute();
  }

  /**
   * Execute une ligne de commande dans un processus separe en specifiant des
   * variables d'environnement.
   * 
   * @param command ligne de commande
   * @param envp variables d'environnement
   * 
   * @return valeur de retour du processus
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  final public int exec(String command, String[] envp) throws IOException {
    process = Runtime.getRuntime().exec(command, envp);
    return execute();
  }

  /**
   * Execute une ligne de commande dans un processus separe en specifiant des
   * variables d'environnement et le repertoire de travail.
   * 
   * @param command ligne de commande
   * @param envp variables d'environnement
   * @param dir repertoire de travail
   * 
   * @return valeur de retour du processus
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  final public int exec(String command, String[] envp, File dir) throws IOException {
    process = Runtime.getRuntime().exec(command, envp, dir);
    return execute();
  }

  /**
   * Execute.
   * DOCUMENT_ME
   * 
   * @return the int
   */
  private int execute() {
    int status = -1;

    // Consommation des fluxs de sortie standard et d'erreur dans des
    // threads separes.
    if (err == null) {
      try {
        process.getErrorStream().close();
      } catch (IOException e) {
        Log.error(e);
      }
    } else {
      createStreamThread(process.getErrorStream(), err);
    }
    if (out == null) {
      try {
        process.getInputStream().close();
      } catch (IOException e) {
        Log.error(e);
      }
    } else {
      createStreamThread(process.getInputStream(), out);
    }

    // Mapping de l'entree standard de l'application si besoin est.
    if (in != null) {
      createStreamThread(in, process.getOutputStream());
    }

    if (timeout > 0L) {
      Thread processThread = createProcessThread(process);
      processThread.start();
      try {
        processThread.join(timeout);
        try {
          status = process.exitValue();
        } catch (IllegalThreadStateException itse) {
          process.destroy();
          status = process.exitValue();
        }
      } catch (InterruptedException ie) {
        Log.error(ie);
      }
    } else if (timeout == 0L) {
      try {
        status = process.waitFor();
      } catch (InterruptedException ie) {
        Log.error(ie);
      }
    }
    finished = true;
    return status;
  }

  /**
   * Creates the stream thread.
   * DOCUMENT_ME
   * 
   * @param is DOCUMENT_ME
   * @param os DOCUMENT_ME
   */
  private void createStreamThread(final InputStream is, final OutputStream os) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = null;
        if (os != null) {
          bos = new BufferedOutputStream(os);
        }
        byte[] data = new byte[2048];
        int nbRead = 0;
        try {
          while ((nbRead = bis.read(data)) > 0) {
            if (bos != null) {
              if (finished) {
                break;
              }
              bos.write(data, 0, nbRead);
              bos.flush();
            }
          }
        } catch (IOException ioe) {
          Log.error(ioe);
        }
      }
    }, "Create Stream Thread").start();
  }

  /**
   * Creates the process thread.
   * DOCUMENT_ME
   * 
   * @param process DOCUMENT_ME
   * 
   * @return the thread
   */
  private Thread createProcessThread(final Process process) {
    return new Thread("Process Watcher Thread") {
      @Override
      public void run() {
        try {
          process.waitFor();
        } catch (InterruptedException ie) {
          Log.error(ie);
        }
      }
    };
  }

  /**
   * Renvoie l'OutputStream vers lequel a ete redirige le flux d'erreur de
   * l'application externe.
   * 
   * @return l'OutputStream vers lequel a ete redirige le flux d'erreur de
   * l'application externe
   */
  public OutputStream getErrorStream() {
    return err;
  }

  /**
   * Renvoie l'InputStream duquel les donnees sont envoyees au flux d'entree
   * standard de l'application externe.
   * 
   * @return l'InputStream duquel les donnees sont envoyees au flux d'entree
   * standard de l'application externe
   */
  public InputStream getInputStream() {
    return in;
  }

  /**
   * Renvoie l'OutputStream vers lequel a ete redirige le flux de sortie
   * standard de l'application externe.
   * 
   * @return l'OutputStream vers lequel a ete redirige le flux de sortie
   * standard de l'application externe
   */
  public OutputStream getOutputStream() {
    return out;
  }

  /**
   * Renvoie le timeout.
   * 
   * @return le timeout
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * Specifie l'Outputstream vers lequel sera redirige la sortie d'erreur de
   * l'application externe.
   * 
   * @param err Outputstream vers lequel sera redirige la sortie d'erreur de
   * l'application externe (null pour ne pas rediriger)
   */
  public void setErrorStream(OutputStream err) {
    this.err = err;
  }

  /**
   * Specifie l'InputStream vers lequel sera redirige l'entree standard de
   * l'application externe.
   * 
   * @param in InputStream vers lequel sera redirige l'entree standard de
   * l'application externe (null pour ne pas rediriger)
   */
  public void setInputStream(InputStream in) {
    this.in = in;
  }

  /**
   * Specifie l'Outputstream vers lequel sera redirige la sortie standard de
   * l'application externe.
   * 
   * @param out Outputstream vers lequel sera redirige la sortie standard de
   * l'application externe (null pour ne pas rediriger)
   */
  public void setOutputStream(OutputStream out) {
    this.out = out;
  }

  /**
   * Specifie le timeout temps en millisecondes avant de forcer l'arret de
   * l'application externe.
   * 
   * @param timeout temps en millisecondes avant de forcer l'arret de l'application
   * externe (0 pour ne jamais forcer l'arret)
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
}
