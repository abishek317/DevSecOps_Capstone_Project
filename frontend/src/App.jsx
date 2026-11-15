import React, { useEffect, useState } from "react";
const API = "/api";
async function request(path, options = {}) {
  const res = await fetch(API + path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  if (res.status === 204) return null;
  const body = await res.json().catch(() => ({ message: res.statusText }));
  if (!res.ok)
    throw new Error(
      body.message || body.error || `Request failed (${res.status})`,
    );
  return body;
}
export default function App() {
  const [user, setUser] = useState(null),
    [tab, setTab] = useState("tasks"),
    [tasks, setTasks] = useState([]),
    [habits, setHabits] = useState([]),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const [authMode, setAuthMode] = useState("login");
  async function refresh() {
    const [t, h] = await Promise.all([request("/tasks"), request("/habits")]);
    setTasks(t);
    setHabits(h);
  }
  useEffect(() => {
    request("/auth/me")
      .then((u) => {
        setUser(u);
        return refresh();
      })
      .catch(() => {});
  }, []);
  async function auth(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    const fd = new FormData(e.currentTarget);
    try {
      const u = await request("/auth/" + authMode, {
        method: "POST",
        body: JSON.stringify({
          email: fd.get("email"),
          password: fd.get("password"),
          displayName: fd.get("displayName"),
        }),
      });
      setUser(u);
      await refresh();
    } catch (ex) {
      setError(ex.message);
    } finally {
      setBusy(false);
    }
  }
  async function logout() {
    await request("/auth/logout", { method: "POST" });
    setUser(null);
    setTasks([]);
    setHabits([]);
  }
  async function submitTask(e) {
    e.preventDefault();
    const form = e.currentTarget;
    const fd = new FormData(form);
    try {
      await request("/tasks", {
        method: "POST",
        body: JSON.stringify({
          title: fd.get("title"),
          description: fd.get("description"),
          dueDate: fd.get("dueDate") || null,
        }),
      });
      form.reset();
      await refresh();
    } catch (ex) {
      setError(ex.message);
    }
  }
  async function submitHabit(e) {
    e.preventDefault();
    const form = e.currentTarget;
    const fd = new FormData(form);
    try {
      await request("/habits", {
        method: "POST",
        body: JSON.stringify({
          name: fd.get("name"),
          description: fd.get("description"),
        }),
      });
      form.reset();
      await refresh();
    } catch (ex) {
      setError(ex.message);
    }
  }
  async function toggleTask(t) {
    try {
      await request(`/tasks/${t.id}/complete?completed=${!t.completed}`, {
        method: "PATCH",
      });
      await refresh();
    } catch (ex) {
      setError(ex.message);
    }
  }
  async function removeTask(id) {
    if (!confirm("Delete this task?")) return;
    try {
      await request("/tasks/" + id, { method: "DELETE" });
      await refresh();
    } catch (ex) {
      setError(ex.message);
    }
  }
  async function completeHabit(h) {
    try {
      const result = await request(`/habits/${h.id}/complete`, {
        method: "POST",
      });
      setError(
        `Habit recorded! Current streak: ${result.currentStreak} day(s).`,
      );
      await refresh();
    } catch (ex) {
      setError(ex.message);
    }
  }
  async function removeHabit(id) {
    if (!confirm("Delete this habit and its completion history?")) return;
    try {
      await request("/habits/" + id, { method: "DELETE" });
      await refresh();
    } catch (ex) {
      setError(ex.message);
    }
  }
  if (!user)
    return (
      <main className="auth-shell">
        <section className="auth-card">
          <div className="brand-mark">HF</div>
          <p className="eyebrow">YOUR PERSONAL ROUTINE</p>
          <h1>
            {authMode === "login" ? "Welcome back" : "Create your account"}
          </h1>
          <p className="muted">Build consistency, one small step at a time.</p>
          <form onSubmit={auth} className="stack">
            {authMode === "register" && (
              <label>
                Display name
                <input
                  name="displayName"
                  required
                  minLength="2"
                  placeholder="Your name"
                />
              </label>
            )}
            <label>
              Email
              <input
                name="email"
                type="email"
                required
                placeholder="you@example.com"
              />
            </label>
            <label>
              Password
              <input
                name="password"
                type="password"
                required
                minLength="8"
                placeholder="At least 8 characters"
              />
            </label>
            <button disabled={busy}>
              {busy
                ? "Please wait…"
                : authMode === "login"
                  ? "Log in"
                  : "Register"}
            </button>
          </form>
          {error && <p className="error">{error}</p>}
          <p className="switch">
            {authMode === "login"
              ? "Don't have an account?"
              : "Already registered?"}{" "}
            <button
              className="link"
              onClick={() => {
                setAuthMode(authMode === "login" ? "register" : "login");
                setError("");
              }}
            >
              {authMode === "login" ? "Create one" : "Log in"}
            </button>
          </p>
        </section>
      </main>
    );
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">HF</span>
          <div>
            <b>HabitFlow</b>
            <small>Task & habit planner</small>
          </div>
        </div>
        <div className="nav-label">WORKSPACE</div>
        <button
          className={tab === "tasks" ? "nav active" : "nav"}
          onClick={() => setTab("tasks")}
        >
          ▣ <span>My tasks</span>
          <em>{tasks.filter((t) => !t.completed).length}</em>
        </button>
        <button
          className={tab === "habits" ? "nav active" : "nav"}
          onClick={() => setTab("habits")}
        >
          ✦ <span>My habits</span>
        </button>
        <div className="sidebar-bottom">
          <div className="avatar">{user.displayName?.[0]?.toUpperCase()}</div>
          <div className="user-info">
            <b>{user.displayName}</b>
            <small>{user.email}</small>
          </div>
          <button className="logout" title="Log out" onClick={logout}>
            ↪
          </button>
        </div>
      </aside>
      <main className="content">
        <header>
          <div>
            <p className="eyebrow">PERSONAL DASHBOARD</p>
            <h1>{tab === "tasks" ? "Your tasks" : "Your habits"}</h1>
            <p className="muted">
              {tab === "tasks"
                ? "Make progress on what matters today."
                : "Small actions, repeated daily, create lasting change."}
            </p>
          </div>
          <div className="date-pill">
            {new Date().toLocaleDateString(undefined, {
              weekday: "short",
              month: "short",
              day: "numeric",
            })}
          </div>
        </header>
        {error && (
          <div className="notice" onClick={() => setError("")}>
            {error} <span>×</span>
          </div>
        )}
        {tab === "tasks" ? (
          <div className="grid">
            <section className="panel">
              <div className="panel-heading">
                <div>
                  <h2>Add a task</h2>
                  <p className="muted">Capture your next action.</p>
                </div>
                <span className="icon">＋</span>
              </div>
              <form className="stack" onSubmit={submitTask}>
                <label>
                  Task title
                  <input
                    name="title"
                    required
                    maxLength="160"
                    placeholder="e.g. Finish project report"
                  />
                </label>
                <label>
                  Description
                  <textarea
                    name="description"
                    maxLength="1000"
                    rows="3"
                    placeholder="Add a little context…"
                  />
                </label>
                <label>
                  Due date
                  <input name="dueDate" type="date" />
                </label>
                <button>Add task</button>
              </form>
            </section>
            <section className="panel list-panel">
              <div className="panel-heading">
                <div>
                  <h2>Task list</h2>
                  <p className="muted">
                    {tasks.length} total ·{" "}
                    {tasks.filter((t) => t.completed).length} completed
                  </p>
                </div>
                <span className="icon">▤</span>
              </div>
              {tasks.length === 0 ? (
                <div className="empty">
                  <div>☀</div>
                  <b>Your list is clear</b>
                  <p>Add a task to get started.</p>
                </div>
              ) : (
                <div className="items">
                  {tasks.map((t) => (
                    <article
                      className={"item " + (t.completed ? "done" : "")}
                      key={t.id}
                    >
                      <button className="check" onClick={() => toggleTask(t)}>
                        {t.completed ? "✓" : ""}
                      </button>
                      <div className="item-main">
                        <b>{t.title}</b>
                        {t.description && <p>{t.description}</p>}
                        {t.dueDate && <small>Due {t.dueDate}</small>}
                      </div>
                      <button
                        className="delete"
                        onClick={() => removeTask(t.id)}
                        title="Delete task"
                      >
                        ×
                      </button>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </div>
        ) : (
          <div className="grid">
            <section className="panel">
              <div className="panel-heading">
                <div>
                  <h2>Create a habit</h2>
                  <p className="muted">Choose something worth repeating.</p>
                </div>
                <span className="icon">✦</span>
              </div>
              <form className="stack" onSubmit={submitHabit}>
                <label>
                  Habit name
                  <input
                    name="name"
                    required
                    maxLength="160"
                    placeholder="e.g. Read for 20 minutes"
                  />
                </label>
                <label>
                  Description
                  <textarea
                    name="description"
                    maxLength="500"
                    rows="3"
                    placeholder="What does success look like?"
                  />
                </label>
                <button>Add habit</button>
              </form>
            </section>
            <section className="panel list-panel">
              <div className="panel-heading">
                <div>
                  <h2>Your habits</h2>
                  <p className="muted">Track daily consistency.</p>
                </div>
                <span className="icon">♨</span>
              </div>
              {habits.length === 0 ? (
                <div className="empty">
                  <div>✦</div>
                  <b>No habits yet</b>
                  <p>Create your first habit.</p>
                </div>
              ) : (
                <div className="items">
                  {habits.map((h) => (
                    <article className="item habit-item" key={h.id}>
                      <div className="habit-symbol">✦</div>
                      <div className="item-main">
                        <b>{h.name}</b>
                        {h.description && <p>{h.description}</p>}
                        <div className="streaks">
                          <span>🔥 {h.currentStreak} day streak</span>
                          <span>Best: {h.longestStreak}</span>
                        </div>
                      </div>
                      <button
                        className="complete-habit"
                        disabled={h.completedToday}
                        onClick={() => completeHabit(h)}
                      >
                        {h.completedToday ? "Done today" : "Complete"}
                      </button>
                      <button
                        className="delete"
                        onClick={() => removeHabit(h.id)}
                        title="Delete habit"
                      >
                        ×
                      </button>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </div>
        )}
        <footer>
          HabitFlow <span>·</span> Stay consistent, not perfect.
        </footer>
      </main>
    </div>
  );
}
