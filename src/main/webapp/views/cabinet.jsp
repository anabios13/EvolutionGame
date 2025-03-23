<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page import="game.controller.GameManager" %>
<%@ page import="java.lang.String" %>

<html>
<body>
<h1>Welcome, <%=session.getAttribute("player")%>
</h1><br>

<% GameManager gh=(GameManager)application.getAttribute("gameManager");
 String id;
 String name=(String)session.getAttribute("player");
 if (session.getAttribute("gameId")==null ) id="no game";
 else id=String.valueOf(session.getAttribute("gameId"));%>


Your current game: <div><%=id%></div>
Your saved games:<div><%=gh.loadSavedGames(name)%></div>
${loadError}<br>
<form method="post" action="load">
    Input number of game:<input type="text" name="gameId"><button type="submit">Load game</button>
</form>
Games you can join: <div><%=gh.getNewGames(name) %></div>

${joinError}<br>
<form method="post" action="start">
    Input number of game:<input type="text" name="gameId"><button type="submit">Join game</button>
</form>

${createError}<br/>
<form method="post" action="create">
    <input type="radio" name="number" value="2" checked> 2<br>
    <input type="radio" name="number" value="3"> 3<br>
    <input type="radio" name="number" value="4"> 4<br>
    <input type="submit" value="Create new game"/>
</form>


<form action="signOut">
    <input type="submit" value="Sign Out"/>
</form>
</body>
</html>
