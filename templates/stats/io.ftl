<html>
  <head>
    <title>Hello</title>
  </head>
  <body>
    <h1>AJAX Server Statistics - IO</h1>
    <#--
    <#list channels?keys as channel>
    <h2>${channel}</h2>
    <table>
     <tr>
     <th>Event</th>
     <th>Time</th>
     </tr>
     <#list channels[channel] as event>
      <tr>
        <td>${event.event}</td>
        <td>${event.time}</td>
      <tr>
     </#list>

     </table>
    </#list>
    -->

    <#list topics as topic>
      <img src="stats/${topic}" width="400" height="300"/><br/><br/>
    </#list>
    </body>
</html>