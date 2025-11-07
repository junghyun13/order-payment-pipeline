let token = null;

// 화면 전환 함수
function showLogin() {
    document.getElementById("initial-screen").style.display = "none";
    document.getElementById("signup-screen").style.display = "none";
    document.getElementById("login-screen").style.display = "block";
    document.getElementById("logout-screen").style.display = "none";
    document.getElementById("find-screen").style.display = "none"; // 추가됨
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

let emailVerified = false; // 회원가입용 이메일 인증 여부
let resetEmailVerified = false; // ✅ 비밀번호 재설정용 이메일 인증 여부 (추가됨)

// 이메일 인증 코드 전송
function sendEmailCode() { // 추가됨
    const email = document.getElementById("signup-email").value;
    fetch('/auth/send-code?email=' + encodeURIComponent(email), { method: 'POST' })
    .then(res => {
        if (!res.ok) throw new Error("이메일 발송 실패: " + res.status);
        return res.text();
    })
    .then(msg => {
        document.getElementById("signup-msg").innerText = "인증 코드 발송됨";
    })
    .catch(err => {
        document.getElementById("signup-msg").innerText = err.message;
    });
}

// ✅ 1️⃣ 아이디 찾기 이메일 발송
function sendFindCode() {
    const email = document.getElementById("find-email").value;
    fetch('/auth/send-code-find?email=' + encodeURIComponent(email), { method: 'POST' })
        .then(res => res.text())
        .then(msg => {
            document.getElementById("find-msg").innerText = msg;
        })
        .catch(err => {
            document.getElementById("find-msg").innerText = err.message;
        });
}


// 이메일 코드 인증
function verifyEmailCode() {
    const email = document.getElementById("signup-email").value;
    const code = document.getElementById("signup-code").value;
    fetch('/auth/verify-email?email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code), { method: 'POST' })
    .then(res => res.text())
    .then(msg => {
        document.getElementById("signup-msg").innerText = msg;

        if (msg.includes("성공")) {
            emailVerified = true;
            // ✅ 인증 성공 시 회원가입 버튼 표시
            document.getElementById("signup-btn").style.display = "inline-block";
        } else {
            emailVerified = false;
            document.getElementById("signup-btn").style.display = "none";
        }
        // ✅ username/password 입력 활성화/비활성화
                document.getElementById("signup-username").disabled = !msg.includes("성공");

                document.getElementById("signup-password").disabled = !msg.includes("성공");
    })
    .catch(err => {
        document.getElementById("signup-msg").innerText = err.message;
    });
}


// 회원가입 버튼 클릭 시 인증 확인 + Gmail 체크
function signup() {
    const username = document.getElementById("signup-username").value;
    const password = document.getElementById("signup-password").value;
    const email = document.getElementById("signup-email").value;

    // 1️⃣ 이메일 인증 완료 여부 확인
    if (!emailVerified) {
        document.getElementById("signup-msg").innerText = "이메일 인증을 먼저 완료해주세요!";
        return;
    }

    // 2️⃣ Gmail만 허용 체크
    if (!email.endsWith("@gmail.com")) {
        document.getElementById("signup-msg").innerText = "Gmail 계정만 가입 가능합니다!";
        return;
    }

    fetch('/auth/signup', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, password, email})
        })
        .then(res => res.text())
        .then(msg => {
            document.getElementById("signup-msg").innerText = msg;

            if (msg.includes("성공")) {
                showLogin(); // ✅ 기존 showLogoutScreen() 대신 showLogin() 호출
                document.getElementById("login-msg").innerText = "회원가입 성공! 로그인 해주세요.";
            }
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
        showLogoutScreen();
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
        document.getElementById("initial-screen").style.display = "block";
    })
    .catch(err => {
        document.getElementById("logout-msg").innerText = err.message;
    });
}

// 아이디/비밀번호 찾기 화면으로 이동 // 추가됨
function showFindScreen() {
    document.getElementById("initial-screen").style.display = "none";
    document.getElementById("login-screen").style.display = "none";
    document.getElementById("signup-screen").style.display = "none";
    document.getElementById("logout-screen").style.display = "none";
    document.getElementById("find-screen").style.display = "block";
}

// 아이디 찾기 + 비밀번호 재설정 // 추가됨
function findAndReset() {
    const email = document.getElementById("find-email").value;
    const newPassword = document.getElementById("find-new-password").value;

    // 1️⃣ 아이디 찾기
    fetch(`/auth/find-username?email=${encodeURIComponent(email)}`, { method: 'POST' })
    .then(res => {
        if (!res.ok) throw new Error("아이디 찾기 실패: " + res.status);
        return res.text();
    })
    .then(usernameMsg => {
        document.getElementById("find-msg").innerText = usernameMsg;

        // 2️⃣ 비밀번호 재설정
        return fetch(`/auth/reset-password?email=${encodeURIComponent(email)}&newPassword=${encodeURIComponent(newPassword)}`, { method: 'POST' });
    })
    .then(res => {
        if (!res.ok) throw new Error("비밀번호 재설정 실패: " + res.status);
        return res.text();
    })
    .then(resetMsg => {
        document.getElementById("find-msg").innerText += "\n" + resetMsg;
        if (resetMsg.includes("성공")) showLogoutScreen(); // 성공시 로그아웃 화면으로 이동
    })
    .catch(err => {
        document.getElementById("find-msg").innerText = err.message;
    });
}

// ✅ 비밀번호 재설정 (이메일 인증 추가됨)
// ============================

// 아이디/비밀번호 찾기 화면으로 이동
function showFindScreen() {
    document.getElementById("initial-screen").style.display = "none";
    document.getElementById("login-screen").style.display = "none";
    document.getElementById("signup-screen").style.display = "none";
    document.getElementById("logout-screen").style.display = "none";
    document.getElementById("find-screen").style.display = "block";
}

// ✅ 1️⃣ 비밀번호 재설정 코드 전송
function sendResetCode() {
    const email = document.getElementById("find-email").value;
    fetch('/auth/send-code?email=' + encodeURIComponent(email), { method: 'POST' })
        .then(res => res.text())
        .then(msg => {
            document.getElementById("find-msg").innerText = "인증 코드가 이메일로 발송되었습니다.";
            document.getElementById("verify-section").style.display = "block";
        })
        .catch(err => {
            document.getElementById("find-msg").innerText = err.message;
        });
}

// ✅ 2️⃣ 이메일 코드 인증
function verifyResetCode() {
    const email = document.getElementById("find-email").value;
    const code = document.getElementById("find-code").value;
    fetch('/auth/verify-email?email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code), { method: 'POST' })
        .then(res => res.text())
        .then(msg => {
            if (msg.includes("성공")) {
                resetEmailVerified = true;
                document.getElementById("find-msg").innerText = "이메일 인증 성공! 새 비밀번호를 입력하세요.";
                document.getElementById("reset-section").style.display = "block";
            } else {
                document.getElementById("find-msg").innerText = "이메일 인증 실패. 다시 시도해주세요.";
            }
        })
        .catch(err => {
            document.getElementById("find-msg").innerText = err.message;
        });
}

// ✅ 3️⃣ 인증 완료 후 비밀번호 재설정
function resetPassword() {
    if (!resetEmailVerified) {
        document.getElementById("find-msg").innerText = "먼저 이메일 인증을 완료해주세요!";
        return;
    }

    const email = document.getElementById("find-email").value;
    const newPassword = document.getElementById("find-new-password").value;

    fetch(`/auth/reset-password?email=${encodeURIComponent(email)}&newPassword=${encodeURIComponent(newPassword)}`, { method: 'POST' })
        .then(res => res.text())
        .then(msg => {
            document.getElementById("find-msg").innerText = msg;
            if (msg.includes("성공")) showLogoutScreen();
        })
        .catch(err => {
            document.getElementById("find-msg").innerText = err.message;
        });
}