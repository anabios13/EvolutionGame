<html>
<body>
<h2>Welcome to Evolution online</h2><br/>
Sign Up<br/>
<form method="post" action="signUp">
${signUpError}<br/>
    login: <input type="text" name="login"><br />
        password: <input type="password" name="password">
<button type="submit">Sign Up</button>
</form>
Sign In<br/>
<form method="post" action="signIn">
    ${signInError}<br/>
    login: <input type="text" name="login"><br />
    password: <input type="password" name="password">
    <button type="submit">Sign In</button>
</form>

</body>
</html>
