package net.sf.dz3.device.sensor.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.ThreadContext;
import org.junit.Test;

import net.sf.dz3.device.sensor.AnalogSensor;
import com.homeclimatecontrol.jukebox.datastream.signal.model.DataSample;

public class MedianSetFilterTest {

    private final static double DELTA = 0.000001;

    @Test
    public void oddArray1() {

        AnalogSensor s = mock(AnalogSensor.class);
        Set<AnalogSensor> source = new HashSet<>();

        source.add(s);
        MedianSetFilter msf = new MedianSetFilter("address", source);

        Double[] samples = { 1d };
        double result = msf.filter(samples);

        assertEquals(1d, result, DELTA);
    }

    @Test
    public void oddArray3() {

        AnalogSensor s = mock(AnalogSensor.class);
        Set<AnalogSensor> source = new HashSet<>();

        source.add(s);
        MedianSetFilter msf = new MedianSetFilter("address", source);

        Double[] samples = { 1d, 15d, 17d };
        double result = msf.filter(samples);

        assertEquals(15d, result, DELTA);
    }

    @Test
    public void evenArray2() {

        AnalogSensor s = mock(AnalogSensor.class);
        Set<AnalogSensor> source = new HashSet<>();

        source.add(s);
        MedianSetFilter msf = new MedianSetFilter("address", source);

        Double[] samples = { 1d, 3d };
        double result = msf.filter(samples);

        assertEquals(2d, result, DELTA);
    }

    @Test
    public void evenArray4() {

        AnalogSensor s = mock(AnalogSensor.class);
        Set<AnalogSensor> source = new HashSet<>();

        source.add(s);
        MedianSetFilter msf = new MedianSetFilter("address", source);

        Double[] samples = { 1d, 3d, 4d, 5d };
        double result = msf.filter(samples);

        assertEquals(3.5d, result, DELTA);
    }

    @Test
    public void consume() {

        ThreadContext.push("consume");

        try {

            String a1 = "a1";
            String a2 = "a2";
            String a3 = "a3";

            // VT: NOTE: Mocks would do great here, except they don't support mocking final methods

            AnalogSensor s1 = new NullSensor(a1, 1000);
            AnalogSensor s2 = new NullSensor(a2, 1000);
            AnalogSensor s3 = new NullSensor(a3, 1000);

            Set<AnalogSensor> source = new HashSet<>();

            source.add(s1);
            source.add(s2);
            source.add(s3);

            MedianSetFilter msf = new MedianSetFilter("address", source);

            // Happy path

            // 2, null, null
            msf.consume(new DataSample<Double>(1, a1, a1, 2d, null));
            assertEquals(1, msf.getSignal().timestamp);
            assertEquals(2d, msf.getSignal().sample, DELTA);

            // 2, 1, null
            msf.consume(new DataSample<Double>(2, a2, a2, 1d, null));
            assertEquals(2, msf.getSignal().timestamp);
            assertEquals(1.5d, msf.getSignal().sample, DELTA);

            // 2, 1, 5
            msf.consume(new DataSample<Double>(3, a3, a3, 5d, null));
            assertEquals(3, msf.getSignal().timestamp);
            assertEquals(2d, msf.getSignal().sample, DELTA);

            // 2, 1, 4
            msf.consume(new DataSample<Double>(4, a3, a3, 4d, null));
            assertEquals(4, msf.getSignal().timestamp);
            assertEquals(2d, msf.getSignal().sample, DELTA);

            // 2, 1, 1.5
            msf.consume(new DataSample<Double>(5, a3, a3, 1.5d, null));
            assertEquals(5, msf.getSignal().timestamp);
            assertEquals(1.5d, msf.getSignal().sample, DELTA);

            // 1, 1, 1.5
            msf.consume(new DataSample<Double>(6, a1, a1, 1d, null));
            assertEquals(6, msf.getSignal().timestamp);
            assertEquals(1d, msf.getSignal().sample, DELTA);

            // Unhappy path

            // null, 1, 1.5
            msf.consume(new DataSample<Double>(7, a1, a1, null, new Error(a1)));
            assertNotNull(msf.getSignal());
            assertNotNull(msf.getSignal().sample);
            assertEquals(1.25d, msf.getSignal().sample, DELTA);

            // null, null, 1.5
            msf.consume(new DataSample<Double>(8, a2, a2, null, new Error(a1)));
            assertNotNull(msf.getSignal());
            assertNotNull(msf.getSignal().sample);
            assertEquals(1.5d, msf.getSignal().sample, DELTA);

            // null, null, null
            msf.consume(new DataSample<Double>(9, a3, a3, null, new Error(a1)));
            assertNotNull(msf.getSignal());
            assertNull(msf.getSignal().sample);

            try {

                msf.consume(null);
                fail("Should've failed already");

            } catch (IllegalArgumentException ex) {
                assertEquals("sample can't be null", ex.getMessage());
            }

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    public void expire() {

        ThreadContext.push("expire");

        try {

            String a1 = "a1";
            String a2 = "a2";
            String a3 = "a3";

            // VT: NOTE: Mocks would do great here, except they don't support mocking final methods

            AnalogSensor s1 = new NullSensor(a1, 1000);
            AnalogSensor s2 = new NullSensor(a2, 1000);
            AnalogSensor s3 = new NullSensor(a3, 1000);

            Set<AnalogSensor> source = new HashSet<>();

            source.add(s1);
            source.add(s2);
            source.add(s3);

            MedianSetFilter msf = new MedianSetFilter("address", source, 1000);

            // 0, 2, null, null
            msf.consume(new DataSample<Double>(0, a1, a1, 2d, null));
            assertEquals(2d, msf.getSignal().sample, DELTA);

            // 300, 1, null
            msf.consume(new DataSample<Double>(300, a2, a2, 1d, null));
            assertEquals(1.5d, msf.getSignal().sample, DELTA);

            // 600, 1, 5
            msf.consume(new DataSample<Double>(600, a3, a3, 5d, null));
            assertEquals(2d, msf.getSignal().sample, DELTA);

            // 900, 2, 1, 4
            msf.consume(new DataSample<Double>(4, a3, a3, 4d, null));
            assertEquals(2d, msf.getSignal().sample, DELTA);

            // This one will expire a1 and a2
            // 1500, null, null, 1.25
            msf.consume(new DataSample<Double>(1500, a3, a3, 1.28d, null));
            assertEquals(1.28d, msf.getSignal().sample, DELTA);

        } finally {
            ThreadContext.pop();
        }
    }
}
