<?xml version='1.0' encoding='ISO-8859-1'?>
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
                         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">

<helpset version="2.0" xml:lang="en">
	<title>StreamFlow Beta</title>
	<maps>
		<homeID>intro1</homeID>
		<mapref location="map.xml"/>
	</maps>
	<view mergetype="javax.help.AppendMerge">
		<name>TOC</name>
		<label>Innehåll</label>
		<type>javax.help.TOCView</type>
		<data>toc.xml</data>
		<image>folder2_green</image>
	</view>
	<view mergetype="javax.help.AppendMerge">
		<name>Index</name>
		<label>Index</label>
		<type>javax.help.IndexView</type>
		<data>index.xml</data>
		<image>index_view</image>
	</view>
	<view>
		<name>Favorites</name>
		<label>Bokmärken</label>
		<type>javax.help.FavoritesView</type>
		<image>star_yellow</image>
	</view>
	<view>
		<name>Search</name>
		<label>Search</label>
		<type>javax.help.SearchView</type>
		<data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
	</view>
	<presentation default="true" displayviews="true" displayviewimages="true">
		<name>StreamFlow</name>
		<size width="800" height="600" />
		<location x="10" y="10" />
		<title>StreamFlow Help Beta</title>
		<image>help2</image>
		<toolbar>
			<helpaction image="nav_left_blue1">javax.help.BackAction</helpaction>
			<helpaction image="nav_right_blue1">javax.help.ForwardAction</helpaction>
			<helpaction>javax.help.SeparatorAction</helpaction>
			<helpaction image="printer31">javax.help.PrintAction</helpaction>
			<helpaction image="document_gear1">javax.help.PrintSetupAction</helpaction>
			<helpaction image="nav_refresh_green1">javax.help.ReloadAction</helpaction>
			<helpaction image="house">javax.help.HomeAction</helpaction>
			<helpaction image="star_yellow_add1">javax.help.FavoritesAction</helpaction>
		</toolbar>
	</presentation>
</helpset>
