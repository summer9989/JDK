/*
 * Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.jvm.hotspot.memory;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.classfile.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

public class Dictionary extends sun.jvm.hotspot.utilities.Hashtable {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }

  private static synchronized void initialize(TypeDataBase db) {
    // just checking that the type exists
    Type type = db.lookupType("Dictionary");
  }

  public Dictionary(Address addr) {
    super(addr);
  }

  // this is overriden here so that Hashtable.bucket will return
  // object of DictionaryEntry.class
  protected Class getHashtableEntryClass() {
    return DictionaryEntry.class;
  }

  /** All classes, and their initiating class loader, passed in. */
  public void allEntriesDo(ClassLoaderDataGraph.ClassAndLoaderVisitor v, Oop loader) {
    int tblSize = tableSize();
    for (int index = 0; index < tblSize; index++) {
      for (DictionaryEntry probe = (DictionaryEntry) bucket(index); probe != null;
                                              probe = (DictionaryEntry) probe.next()) {
        Klass k = probe.klass();
        v.visit(k, loader);
      }
    }
  }

  // - Internals only below this point

  private DictionaryEntry getEntry(int index, long hash, Symbol className) {
    for (DictionaryEntry entry = (DictionaryEntry) bucket(index); entry != null;
                                    entry = (DictionaryEntry) entry.next()) {
      if (entry.hash() == hash && entry.equals(className)) {
        return entry;
      }
    }
    return null;
  }

  public boolean contains(Klass c) {
    long hash = computeHash(c.getName());
    int index = hashToIndex(hash);

    for (DictionaryEntry entry = (DictionaryEntry) bucket(index); entry != null;
                                    entry = (DictionaryEntry) entry.next()) {
      if (entry.literalValue().equals(c.getAddress())) {
        return true;
      }
    }
    return false;
  }
}