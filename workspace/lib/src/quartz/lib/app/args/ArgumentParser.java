package quartz.lib.app.args;

import java.util.Deque;
import java.util.List;

import quartz.lib.app.ApplicationVessel;

public interface ArgumentParser {
    List<ApplicationVessel> getApplicationVesselList();
    <T> Deque<T> getValueStack( ArgumentParserStackKey<T> key );
    void registerFactory(String key, ArgumentParserElementFactory value);
    ArgumentParserElementFactory getFactory(String key);
}