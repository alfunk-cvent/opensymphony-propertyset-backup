<html>
<head>
<title>PropertySet Usage and Configuration</title>
</head>

<body>
<p>The PropertSet module is configured by a file that is to be located somewhere
  in the classpath of your application. It looks for the following files, in this
  order:</p>
<ol>
  <li>/propertyset.xml</li>
  <li>/META-INF/propertyset.xml</li>
  <li>/META-INF/propertyset-default.xml</li>
</ol>

<p>The <strong>propertyset-default.xml</strong> file will always be found, as
  it is included in <strong>propertyset.jar</strong>. You can override these configurations
  by writing your own propertyset.xml file and placing it in one of the above
  locations in the classpath. The configuration file must look like this:</p>

<pre><code>&lt;propertysets&gt;
	&lt;propertset name=&quot;baz&quot; class=&quot;com.foo.bar.BazPropertySet&quot;&gt;
		&lt;arg name=&quot;some&quot; value=&quot;thing&quot;/&gt;
		...
	&lt;/propertyset&gt;
	...
&lt;/propertysets&gt;</code></pre>

<p>The code to use the above PropertySet would be:</p>
<pre><code>import com.opensymphony.module.propertyset.*;
...
HashMap args = new HashMap();

// add parameters to the args map
PropertySet ps = PropertySetManager.getIntance(&quot;baz&quot;, args);
</code></pre>
<p>The values that are placed in the <b>args</b> map is specific to the PropertySet
  implementation. We recommend reading the javadocs for the implementation you
  plan to use to discover the required and optional configuration arguments in
  <b>propertyset.xml</b> as well as the runtime arguments passed in via the <b>args
  Map</b> above.<br>
</p>
</body>

</html>