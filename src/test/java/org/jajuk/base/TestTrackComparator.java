package org.jajuk.base;

import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jajuk.TestHelpers;
import org.jajuk.services.startup.StartupCollectionService;

public class TestTrackComparator extends TestCase {
    @Override
    public void setUp() {
        StartupCollectionService.registerItemManagers();
    }

    public void testBuildIdenticalTestFootprint() {
        TrackComparator comparator = new TrackComparator(TrackComparator.TrackComparatorType.ALBUM);
        Track track = createTrack();

        assertEquals("74namename2202010163album-artist", comparator.buildIdenticalTestFootprint(track));
    }

    public void testBuildIdenticalTestFootprintUnknown() {
        TrackComparator comparator = new TrackComparator(TrackComparator.TrackComparatorType.ALBUM);
        Track track = createUnknownTrack();

        assertEquals("unknown10163", comparator.buildIdenticalTestFootprint(track));
    }

    public void testCompareEqualsForAllTypes() {
        Track track = createTrack();
        for (TrackComparator.TrackComparatorType type : TrackComparator.TrackComparatorType.values()) {
            TrackComparator comparator = new TrackComparator(type);

            assertEquals(0, comparator.compare(track, track));
        }
    }

    public void testCompareNotEqualsForAllTypes() {
        Track track = createTrack();
        Track trackUnknown = createUnknownTrack();
        for (TrackComparator.TrackComparatorType type : TrackComparator.TrackComparatorType.values()) {
            TrackComparator comparator = new TrackComparator(type);

            assertTrue(0 != comparator.compare(track, trackUnknown));
        }
    }

    public void testCompareRandom() {
        for (int i = 0;i < 1000;i++) {
            compareRandom();
        }
    }

    private void compareRandom() {
        Track track1 = createRandomTrack();
        Track track2 = createRandomTrack();
        Track track3 = createRandomTrack();

        for (TrackComparator.TrackComparatorType type : TrackComparator.TrackComparatorType.values()) {
            TrackComparator comparator = new TrackComparator(type);

            int ret12 = comparator.compare(track1, track2);
            int ret23 = comparator.compare(track2, track3);
            int ret13 = comparator.compare(track1, track3);

            // perform some advanced check of the comparator to ensure
            // the contract is always correctly implemented

            if (ret12 < 0 && ret23 < 0) {
                assertTrue("If track1 < track2 < track3 also track1 < track3 should hold, but" +
                                "failed for " + type + ": \n" +
                                track1 + "\"" +
                                track2 + "\"" +
                                track3 + "\n",
                        ret13 < 0);
            } else if (ret12 > 0 && ret23 > 0) {
                assertTrue("If track1 > track2 > track3 also track1 > track3 should hold, but" +
                                "failed for " + type + ": \n" +
                                track1 + "\"" +
                                track2 + "\"" +
                                track3 + "\n",
                        ret13 > 0);
            } else if (ret12 == 0 && ret23 == 0) {
                assertEquals("If track1 == track2 == track3 also track1 == track3 should hold, but" +
                        "failed for " + type + ": \n" +
                        track1 + "\"" +
                        track2 + "\"" +
                        track3 + "\n",
                        0, ret13);
            }

            assertEquals("Comparing track1 to track2 should result in the same as comparing track2 to track1",
                    ret12 * -1, comparator.compare(track2, track1));
            assertEquals("Comparing track2 to track3 should result in the same as comparing track3 to track2",
                    ret23 * -1, comparator.compare(track3, track2));

        }
    }

    @SuppressWarnings("unused")
    public void disableTestMicroBenchmark() {
        Track track = createTrack();
        Track trackUnknown = createUnknownTrack();

        // warmup
        runBenchmark(track, trackUnknown);

        for (int i = 0; i < 20;i++) {
            long start = System.currentTimeMillis();
            runBenchmark(track, trackUnknown);
            System.out.println("Had: " + (System.currentTimeMillis() - start) + "ms");
        }

    }

    private void runBenchmark(Track track, Track trackUnknown) {
        for (TrackComparator.TrackComparatorType type : TrackComparator.TrackComparatorType.values()) {
            TrackComparator comparator = new TrackComparator(type);
            for (int i = 0; i < 100_000; i++) {
                assertEquals(0, comparator.compare(track, track));
                assertTrue(0 != comparator.compare(track, trackUnknown));
            }
        }
    }

    public void testComparator() {
        for (TrackComparator.TrackComparatorType type : TrackComparator.TrackComparatorType.values()) {
            TrackComparator comparator = new TrackComparator(type);
            Track track = createTrack();
            Track equal = createTrack();
            Track notEqual = createUnknownTrack();
            TestHelpers.ComparatorTest(comparator, track, equal,
                    notEqual, comparator.compare(track, notEqual) > 0);
        }
    }
    private Track createTrack() {
        Album album = new Album("1", "name", 2);
        Genre genre = new Genre("7", "name7");
        Artist artist = new Artist("4", "name4");
        Year year = new Year("5", "2020");
        Type type = new Type("6", "name6", "ext", null, null);
        Track track = new Track("2", "name2", album, genre, artist, 10,
                year, 1, type, 3);
        track.setAlbumArtist(new AlbumArtist("8", "album-artist"));
        return track;
    }

    private Track createUnknownTrack() {
        Album album = new Album("1", "unknown", 2);
        Genre genre = new Genre("7", "unknown");
        Artist artist = new Artist("4", "unknown");
        Year year = new Year("5", "unknown");
        Type type = new Type("6", "unknown", "ext", null, null);
        Track track = new Track("2", "unknown", album, genre, artist, 10,
                year, 1, type, 3);
        track.setAlbumArtist(new AlbumArtist("8", "unknown"));
        return track;
    }

    private Track createRandomTrack() {
        Album album = new Album("1", RandomStringUtils.random(10), RandomUtils.nextInt(0, 1000));
        Genre genre = new Genre("7", RandomStringUtils.random(10));
        Artist artist = new Artist("4", RandomStringUtils.random(10));
        Year year = new Year("5", Integer.toString(RandomUtils.nextInt(2000, 3000)));
        Type type = new Type("6", RandomStringUtils.random(10), "ext", null, null);
        Track track = new Track("2", RandomStringUtils.random(10), album, genre, artist, 10,
                year, RandomUtils.nextInt(0, 1000), type, RandomUtils.nextInt(0, 1000));
        track.setAlbumArtist(new AlbumArtist("8", RandomStringUtils.random(10)));
        return track;
    }
}