<html>

<head>

<title>PropertySet Database Tips</title>

</head>



<body bgcolor="#FFFFFF" text="#000000">

<p>Most PropertySet implementations persist data in a relational database. Because

  the PropertySet module stores a wide variety of data that is unknown at development

  time, the resulting tables are not entirely in optimized (Boyce-Codd) normal

  form. Since the schema design is not &quot;optimized&quot; to any sorta of specific

  data structures, querying on these tables can be very slow if the database isn't

  aware of any functional dependecies. We recommend that you create the following

  indices on the OS_PROPERTYENTRY table, which will speed up queries by up to

  a factor of 20:</p>

<pre><code>CREATE UNIQUE INDEX os_PropertyEntry_keyidx ON os_PropertyEntry( entityName, entityId, keyValue )
CREATE UNIQUE INDEX os_PropertyEntry_allidx ON os_PropertyEntry( entityName, entityId )</code></pre>

<p><i>Please note that these two SQL calls may or may not work with your database

  vendor and you may be required to modify them accordingly. They may or may not

  be specific to the propertyset implementation you are using. These indices provide

  only a general idea of how you can speed up queries on your PropertySet tables.</i></p>

<p>Besides indices, another speed optimization to take in to account is the key

  names that you choose when writing your application. If all your keys all look

  like <i>com.acme.foo</i>, <i>com.acme.bar</i>, and <i>com.acme.baz</i>, your

  database may not be able to properly partition data in the OS_PROPERTYENTRY

  table accurately. It is recommended that your key values be chosen such that

  they are evenly distributed, either by picking names such as <i>foo</i>, <i>bar</i>,

  and <i>baz</i> (essentially removing the common prefix), or my using a reverse

  key naming convention: <i>oof.emca.moc</i>, <i>rab.emca.moc</i>, and <i>zab.emca.moc</i>.

  By properly distributing your keys, your database should be able to have much

  faster access to ProperySets.<br>

</p>

  <%@ include file="navpanel.jsp" %>

</body>

</html>