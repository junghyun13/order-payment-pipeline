let token = null;

// 화면 전환 함수
function showLogin() {
    document.getElementById("initial-screen").style.display = "none";
    document.getElementById("signup-screen").style.display = "none";
    document.getElementById("login-screen").style.display = "block";
    document.getElementById("logout-screen").style.display = "none";
}

function showSignup() {
    document.getElementById("initial-screen").style.display = "none";
    document.getElementById("login-screen").style.display = "none";
    document.getElementById("signup-screen").style.display = "block";
    document.getElementById("logout-screen").style.display = "none";
}

function showLogoutScreen() {
    document.getElementById("initial-screen").style.display = "none";
    document.getElementById("login-screen").style.display = "none";
    document.getElementById("signup-screen").style.display = "none";
    document.getElementById("logout-screen").style.display = "block";
}

function backToLogin() {
    showLogin();
}

// 회원가입
function signup() {
    const username = document.getElementById("signup-username").value;
    const password = document.getElementById("signup-password").value;

    fetch('/auth/signup', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    })
    .then(res => {
        if (!res.ok) throw new Error("회원가입 실패: " + res.status);
        return res.text();
    })
    .then(msg => {
        document.getElementById("signup-msg").innerText = msg;
    })
    .catch(err => {
        document.getElementById("signup-msg").innerText = err.message;
    });
}

// 로그인
function login() {
    const username = document.getElementById("login-username").value;
    const password = document.getElementById("login-password").value;

    fetch('/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    })
    .then(res => {
        if (!res.ok) throw new Error("로그인 실패: " + res.status);
        return res.text();
    })
    .then(t => {
        if (!t || t.startsWith("Error")) {
            document.getElementById("login-msg").innerText = t || "로그인 실패";
            return;
        }
        token = t;
        document.getElementById("login-msg").innerText = "로그인 성공!";
        showLogoutScreen();  // 로그인 성공하면 로그아웃 화면 표시
    })
    .catch(err => {
        document.getElementById("login-msg").innerText = err.message;
    });
}

// 로그아웃
function logout() {
    if (!token) {
        alert("로그인 먼저!");
        return;
    }

    fetch('/auth/logout', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("로그아웃 실패: " + res.status);
        return res.text();
    })
    .then(msg => {
        token = null;
        document.getElementById("logout-msg").innerText = msg;
        document.getElementById("logout-screen").style.display = "none";
        document.getElementById("initial-screen").style.display = "block";  // 초기 화면으로 돌아가기
    })
    .catch(err => {
        document.getElementById("logout-msg").innerText = err.message;
    });
}
