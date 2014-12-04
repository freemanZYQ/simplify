package org.cf.smalivm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.cf.smalivm.exception.UnknownAncestors;
import org.junit.Before;
import org.junit.Test;

public class TestSmaliClassManager {

    private static final String TEST_DIRECTORY = "resources/test";

    private SmaliClassManager manager;

    @Before
    public void getClassManager() throws IOException {
        manager = new SmaliClassManager(TEST_DIRECTORY);
    }

    @Test
    public void TestChildIsInstanceOfParent() throws UnknownAncestors {
        boolean isInstance = manager.isInstance("Lchild_class;", "Lparent_class;");

        assertTrue(isInstance);
    }

    @Test
    public void TestChildIsInstanceOfGrandParent() throws UnknownAncestors {
        boolean isInstance = manager.isInstance("Lchild_class;", "Lgrandparent_class;");

        assertTrue(isInstance);
    }

    @Test
    public void TestParentIsNotInstanceOfChild() throws UnknownAncestors {
        boolean isInstance = manager.isInstance("Lparent_class;", "Lchild_class;");

        assertFalse(isInstance);
    }

    @Test
    public void TestStringIsInstanceOfObject() throws UnknownAncestors {
        boolean isInstance = manager.isInstance(String.class, Object.class);

        assertTrue(isInstance);
    }

    @Test(expected = UnknownAncestors.class)
    public void TestUnknownChildThrowsUnknownAncestors() throws UnknownAncestors {
        manager.isInstance("Lthis_certainly_wont_exists;", "Lparent_class;");
    }

    @Test
    public void TestUnknownParentDoesNotThrowUnknownAncestors() throws UnknownAncestors {
        manager.isInstance("Lchild_class;", "Lthis_certainly_wont_exists;");
    }

}