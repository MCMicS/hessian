package com.caucho.hessian.io;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.Calendar;
import java.util.Locale;

public class SerializerDeserializerTest {

    @Test
    public void testSerializeDeserializeCalendarWithReference() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);

        out.startMessage();
        Calendar today = Calendar.getInstance(Locale.ENGLISH);
        TestUser testUser1 = new TestUser("User1", today);
        TestUser testUser2 = new TestUser("User2", today);
        TestUser[] testUsers = new TestUser[] {testUser1, testUser2};
        out.writeObject(testUsers);

        out.completeMessage();
        out.close();

        byte[] data = bos.toByteArray();

        // Deserialize
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Hessian2Input in = new Hessian2Input(bin);

        in.startMessage();
        final Object readObject = in.readObject(TestUser[].class);

        in.completeMessage();
        in.close();
        bin.close();
        Assert.assertTrue(readObject instanceof TestUser[]);
        final TestUser[] readTestUsers = (TestUser[]) readObject;
        Assert.assertEquals(2, readTestUsers.length);
        Assert.assertEquals(testUser1.name, readTestUsers[0].getName());
        Assert.assertThat(readTestUsers[0].getLastSeen(), Matchers.instanceOf(Calendar.class));
        Assert.assertEquals(testUser1.getLastSeen().getTimeInMillis(), readTestUsers[0].getLastSeen().getTimeInMillis());
        Assert.assertEquals(testUser2.name, readTestUsers[1].getName());
        Assert.assertThat(readTestUsers[1].getLastSeen(), Matchers.instanceOf(Calendar.class));
        Assert.assertEquals(testUser2.getLastSeen().getTimeInMillis(), readTestUsers[1].getLastSeen().getTimeInMillis());
    }

    private static class TestUser implements java.io.Serializable {
        private final String name;

        public String getName() {
            return name;
        }

        public Calendar getLastSeen() {
            return lastSeen;
        }

        private final Calendar lastSeen;

        private TestUser(String name, Calendar lastSeen) {
            this.name = name;
            this.lastSeen = lastSeen;
        }
    }
}
