module goldfish {
	exports com.aific.finances;
	exports com.aific.finances.io;
	exports com.aific.finances.plot;
	exports osxadapter;
	exports com.aific.finances.util;
	exports com.aific.finances.ui.table;
	exports com.aific.finances.ui;

	requires transitive java.desktop;
	requires transitive java.xml;
	requires opencsv;
}