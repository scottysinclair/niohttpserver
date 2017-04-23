<html>
  <head>
    <title>NIO MultiThreaded HTTP Server</title>
    <style>
      body {
        background-color: PowderBlue;
        font-family: sans-serif;
        font-size: 20pt;
      }
      li {
        margin-bottom: 5px;
      }
    </style>
  </head>
  <body>
    <h1>NIO MultiThreaded HTTP Server</h1>
    <p>This is an extremely scalable & efficient HTTP server. It supports the following features:</p>
    <ul>
      <li>Non-blocking IO (java.nio) - allowing a small number of threads to process many many http connections.</li>
      <li>Highly scalable processing pipeline - can configure:
      <ul>
       <li>The number of threads reading  request data from http connections.</li>
       <li>The number of threads executing servlet logic.</li>
       <li>The number of threads writing response data to http connections.</li>
      </ul>
      <li><a href="/stats/io">IO Statistic reports</a> with JFree chart.</li>
      <li>The freemarker template engine for generating HTML content (including this page).</li>
    </ul>
    <h2>Implementation Info</h2>
    <ul>
      <li>A simple state machine implementation is used for parsing the http request data - this is always required when using NIO.</li>
      <li>A simple publish and subscribe mechanism was implemented to gather statistics from the NIO server operation.</li>
      <li>Thread utilities like FutureValue, BlockingFIFOQueue were implemented (java.util.concurrent did not exist yet) .</li>
    </ul>
  </body>
</html>