package org.example.interop;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class PlayerIdsTest {

    @Test
    public void test_should_be_able_to_create_player_ids_with_an_initial_value() {
        PlayerIds playerIds = new PlayerIds(1);
        assertEquals(1, playerIds.getNextId());
    }

    @Test
    public void test_should_be_able_to_get_current_id() {
        PlayerIds playerIds = new PlayerIds(5);
        assertEquals(5, playerIds.getNextId());
    }

    @Test
    public void test_should_be_able_to_atomically_get_current_id_and_increment_it() {
        PlayerIds playerIds = new PlayerIds(1);
        assertEquals(1, playerIds.getNextIdAndIncrement());
        assertEquals(2, playerIds.getNextId());
    }

    @Test
    public void test_should_be_able_to_atomically_get_current_id_and_increment_it_multiple_times() {
        PlayerIds playerIds = new PlayerIds(1);
        assertEquals(1, playerIds.getNextIdAndIncrement());
        assertEquals(2, playerIds.getNextIdAndIncrement());
        assertEquals(3, playerIds.getNextIdAndIncrement());
        assertEquals(4, playerIds.getNextIdAndIncrement());
        assertEquals(5, playerIds.getNextIdAndIncrement());
    }
}
