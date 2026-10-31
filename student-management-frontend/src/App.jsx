
import React, {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  LayoutDashboard,
  Users,
  BookOpen,
  ClipboardList,
  CalendarCheck,
  LogOut,
  Plus,
  Search,
  Pencil,
  Trash2,
  X,
  Menu,
  GraduationCap,
  AlertCircle,
  RefreshCw,
  ShieldCheck,
  UserRound,
  Download,
  CheckCircle,
} from "lucide-react";

import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from "recharts";

import {
  authAPI,
  getUserFromToken,
  clearToken,
  studentAPI,
  courseAPI,
  marksAPI,
  attendanceAPI,
  attendanceSummaryAPI,
} from "./api";
import AITest from "./assets/AITest.jsx";

// --------------------------------------------------
// CONSTANTS
// --------------------------------------------------

const MODULES = {
  students: {
    title: "Students",
    singular: "Student",
    api: studentAPI,
    icon: Users,
  },
  courses: {
    title: "Courses",
    singular: "Course",
    api: courseAPI,
    icon: BookOpen,
  },
  marks: {
    title: "Marks",
    singular: "Mark",
    api: marksAPI,
    icon: ClipboardList,
  },
  attendance: {
    title: "Attendance",
    singular: "Attendance",
    api: attendanceAPI,
    icon: CalendarCheck,
  },
};

const NAVIGATION = [
  { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
  { id: "students", label: "Students", icon: Users },
  { id: "courses", label: "Courses", icon: BookOpen },
  { id: "marks", label: "Marks", icon: ClipboardList },
  { id: "attendance", label: "Attendance", icon: CalendarCheck },
];

const EMPTY_DATA = {
  students: [],
  courses: [],
  marks: [],
  attendance: [],
};

// --------------------------------------------------
// HELPERS
// --------------------------------------------------

function normalizeList(result) {
  if (Array.isArray(result)) return result;
  if (Array.isArray(result?.content)) return result.content;
  if (Array.isArray(result?.data)) return result.data;
  if (result && typeof result === "object") return [result];
  return [];
}

function getId(item) {
  return item?.id ??
    item?.studentId ??
    item?.courseId ??
    item?.markId;
}

function getDisplayValue(item, keys) {
  for (const key of keys) {
    if (item?.[key] !== undefined && item?.[key] !== null) {
      return item[key];
    }
  }
  return "-";
}

function getErrorMessage(error) {
  return error?.message || "Something went wrong.";
}

function formatDate(value) {
  if (!value) return "-";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return value;

  return date.toLocaleDateString();
}

// CSV helper: handles commas, quotes and line breaks.
function csvValue(value) {
  const text = String(value ?? "");
  return `"${text.replace(/"/g, '""')}"`;
}

function downloadCSV(filename, headers, rows) {
  const csv = [
    headers.map(csvValue).join(","),
    ...rows.map((row) => row.map(csvValue).join(",")),
  ].join("\r\n");

  // UTF-8 BOM helps Excel display CSV correctly.
  const blob = new Blob(["\uFEFF" + csv], {
    type: "text/csv;charset=utf-8;",
  });

  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = filename;

  document.body.appendChild(link);
  link.click();
  link.remove();

  URL.revokeObjectURL(url);
}

// --------------------------------------------------
// LOGIN
// --------------------------------------------------

function Login({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const loggedInUser = await authAPI.login(email, password);

      if (!loggedInUser) {
        throw new Error("Unable to read user details from JWT.");
      }

      onLogin(loggedInUser);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-violet-100 via-white to-purple-100 p-5">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-xl p-8">
        <div className="flex justify-center mb-5">
          <div className="bg-violet-600 p-4 rounded-2xl text-white">
            <GraduationCap size={34} />
          </div>
        </div>

        <h1 className="text-3xl font-bold text-center text-gray-900">
          EduManage
        </h1>

        <p className="text-center text-gray-500 mt-2 mb-8">
          Student Management System
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Email address
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-violet-500"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Password
            </label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-violet-500"
            />
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-xl text-sm">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-violet-600 hover:bg-violet-700 text-white font-semibold py-3 rounded-xl disabled:opacity-60"
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <p className="text-xs text-center text-gray-400 mt-6">
          Secure access for Admin, Faculty and Students
        </p>
      </div>
    </div>
  );
}

// --------------------------------------------------
// STAT CARD
// --------------------------------------------------

function StatCard({ title, value, icon: Icon, color }) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
      <div className="flex justify-between items-center">
        <div>
          <p className="text-sm text-gray-500">{title}</p>
          <h2 className="text-3xl font-bold text-gray-900 mt-2">
            {value}
          </h2>
        </div>
        <div className={`p-3 rounded-xl ${color}`}>
          <Icon size={24} />
        </div>
      </div>
    </div>
  );
}

// --------------------------------------------------
// MAIN APP
// --------------------------------------------------

export default function App() {
  const [user, setUser] = useState(() => getUserFromToken());
  const [activePage, setActivePage] = useState("dashboard");
  const [data, setData] = useState(EMPTY_DATA);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({});
  const [saving, setSaving] = useState(false);
  const [actionError, setActionError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [attendancePercentage, setAttendancePercentage] = useState(null);

  const role = String(user?.role || "").toUpperCase();

  const isAdmin = role === "ADMIN";
  const isFaculty = role === "FACULTY";
  const isStudent = role === "STUDENT";

  const visibleNavigation = useMemo(() => {
    if (isStudent) {
      return NAVIGATION.filter((item) =>
        ["dashboard", "students", "marks", "attendance"].includes(item.id)
      );
    }

    return NAVIGATION;
  }, [isStudent]);

  const canAdd =
    isAdmin ||
    (isFaculty &&
      ["courses", "marks", "attendance"].includes(activePage));

  const canEdit = canAdd;
  const canDelete = canAdd;

  const currentModule = MODULES[activePage];

  // ------------------------------------------------
  // LOAD DATA
  // ------------------------------------------------

  const loadData = useCallback(async () => {
    if (!user) return;

    setLoading(true);
    setErrors({});
    setSuccessMessage("");

    const currentRole = String(user.role || "").toUpperCase();

    const nextData = {
      students: [],
      courses: [],
      marks: [],
      attendance: [],
    };

    const nextErrors = {};
    const requests = {};

    if (currentRole === "ADMIN" || currentRole === "FACULTY") {
      requests.students = () => studentAPI.getAll();
      requests.courses = () => courseAPI.getAll();
      requests.marks = () => marksAPI.getAll();
      requests.attendance = () => attendanceAPI.getAll();
    } else if (currentRole === "STUDENT") {
      const studentId = user.studentId;

      if (studentId === null || studentId === undefined) {
        nextErrors.general =
          "Your account does not have a linked student ID.";
      } else {
        requests.students = async () => {
  const profile = await studentAPI.getById(studentId);
  return profile ? [profile] : [];
};

requests.marks = () =>
  marksAPI.getByStudentId(studentId);

requests.attendance = () =>
  attendanceAPI.getByStudentId(studentId);

requests.attendancePercentage = () =>
  attendanceSummaryAPI.getPercentage(studentId);
      }
    } else {
      nextErrors.general =
        `Unsupported role: ${currentRole || "missing role"}`;
    }

    const entries = Object.entries(requests);

    const results = await Promise.allSettled(
      entries.map(([, requestFn]) => requestFn())
    );

    results.forEach((result, index) => {
      const key = entries[index][0];

      if (result.status === "fulfilled") {
        if (key === "attendancePercentage") {
         const value =
  typeof result.value === "number"
    ? result.value
    : result.value?.attendancePercentage ??
      result.value?.percentage ??
      null;

setAttendancePercentage(
  value === null ? null : Number(value)
);
        } else {
          nextData[key] = normalizeList(result.value);
        }
      } else {
        nextErrors[key] = getErrorMessage(result.reason);
      }
    });

    setData(nextData);
    setErrors(nextErrors);
    setLoading(false);
  }, [user]);

  useEffect(() => {
    if (user) loadData();
  }, [user, loadData]);

  // ------------------------------------------------
  // SESSION EXPIRATION
  // ------------------------------------------------

  useEffect(() => {
    const handleExpired = () => {
      clearToken();
      setUser(null);
      setData(EMPTY_DATA);
      setAttendancePercentage(null);
    };

    window.addEventListener("auth-expired", handleExpired);

    return () => {
      window.removeEventListener("auth-expired", handleExpired);
    };
  }, []);

  // ------------------------------------------------
  // LOGIN / LOGOUT
  // ------------------------------------------------

  const handleLogin = (loggedInUser) => {
    setUser(loggedInUser);
    setActivePage("dashboard");
  };

  const handleLogout = () => {
    authAPI.logout();
    setUser(null);
    setData(EMPTY_DATA);
    setErrors({});
    setAttendancePercentage(null);
    setActivePage("dashboard");
  };

  // ------------------------------------------------
  // MODAL
  // ------------------------------------------------

  const openAddModal = () => {
    setEditingItem(null);
    setFormData(
      activePage === "attendance"
        ? {
            attendanceDate: new Date().toISOString().slice(0, 10),
            status: "PRESENT",
          }
        : {}
    );
    setActionError("");
    setModalOpen(true);
  };

  const openEditModal = (item) => {
    setEditingItem(item);
    setFormData({ ...item });
    setActionError("");
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingItem(null);
    setFormData({});
    setActionError("");
  };

  // ------------------------------------------------
  // FORM FIELDS
  // ------------------------------------------------

  const getFormFields = () => {
    if (activePage === "students") {
      return [
        { name: "name", label: "Student Name", type: "text" },
        { name: "email", label: "Email", type: "email" },
        { name: "department", label: "Department", type: "text" },
        { name: "age", label: "Age", type: "number" },
      ];
    }

    if (activePage === "courses") {
      return [
        { name: "courseName", label: "Course Name", type: "text" },
        { name: "courseCode", label: "Course Code", type: "text" },
        { name: "description", label: "Description", type: "text" },
      ];
    }

    if (activePage === "marks") {
      return [
        { name: "studentId", label: "Student ID", type: "number" },
        { name: "subject", label: "Subject", type: "text" },
        { name: "semester", label: "Semester", type: "number" },
        { name: "marksObtained", label: "Marks Obtained", type: "number" },
        { name: "maximumMarks", label: "Maximum Marks", type: "number" },
      ];
    }

    if (activePage === "attendance") {
      return [
        { name: "studentId", label: "Student ID", type: "number" },
        { name: "attendanceDate", label: "Attendance Date", type: "date" },
        {
          name: "status",
          label: "Attendance Status",
          type: "select",
          options: ["PRESENT", "ABSENT", "LEAVE"],
        },
        { name: "remarks", label: "Remarks", type: "text" },
      ];
    }

    return [];
  };

  // ------------------------------------------------
  // SAVE DATA
  // ------------------------------------------------

  const handleSave = async (event) => {
    event.preventDefault();

    if (!currentModule) return;

    setSaving(true);
    setActionError("");
    setSuccessMessage("");

    try {
      const api = currentModule.api;
      const payload = { ...formData };

      // Do not send database-generated IDs.
      delete payload.id;

      if (activePage === "marks") {
        payload.studentId = Number(payload.studentId);
        payload.semester = Number(payload.semester);
        payload.marksObtained = Number(payload.marksObtained);
        payload.maximumMarks = Number(payload.maximumMarks);
      }

      if (activePage === "attendance") {
        payload.studentId = Number(payload.studentId);
      }

      if (activePage === "students") {
        payload.age = Number(payload.age);
      }

      if (editingItem) {
        const id = getId(editingItem);

        if (id === undefined || id === null) {
          throw new Error("Record ID was not found.");
        }

        await api.update(id, payload);
      } else {
        await api.create(payload);
      }

      const savedModule = activePage;
      closeModal();

      await loadData();

      if (savedModule === "marks" || savedModule === "attendance") {
        setSuccessMessage(
          `${savedModule === "marks" ? "Marks" : "Attendance"} saved successfully. The notification request was processed by the backend.`
        );
      } else {
        setSuccessMessage(`${currentModule.singular} saved successfully.`);
      }
    } catch (error) {
      setActionError(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  // ------------------------------------------------
  // DELETE
  // ------------------------------------------------

  const handleDelete = async (item) => {
    if (!currentModule) return;

    const id = getId(item);

    if (id === undefined || id === null) {
      alert("Record ID was not found.");
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete this ${currentModule.singular.toLowerCase()}?`
    );

    if (!confirmed) return;

    try {
      await currentModule.api.remove(id);
      setSuccessMessage(`${currentModule.singular} deleted successfully.`);
      await loadData();
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  // ------------------------------------------------
  // CSV DOWNLOADS - STUDENT
  // ------------------------------------------------

  const downloadMarksCSV = () => {
    const headers = [
      "Student ID",
      "Subject",
      "Semester",
      "Marks Obtained",
      "Maximum Marks",
      "Percentage",
      "Grade",
    ];

    const rows = data.marks.map((mark) => {
      const percentage =
        mark.maximumMarks > 0
          ? (
              (Number(mark.marksObtained) /
                Number(mark.maximumMarks)) *
              100
            ).toFixed(2)
          : "0.00";

      let grade = mark.grade;

      if (!grade) {
        const value = Number(percentage);
        grade =
          value >= 90 ? "A+" :
          value >= 80 ? "A" :
          value >= 70 ? "B" :
          value >= 60 ? "C" :
          value >= 50 ? "D" : "F";
      }

      return [
        mark.studentId,
        mark.subject,
        mark.semester,
        mark.marksObtained,
        mark.maximumMarks,
        percentage,
        grade,
      ];
    });

    downloadCSV("My_Marks.csv", headers, rows);
  };

  const downloadAttendanceCSV = () => {
    const headers = [
      "Student ID",
      "Attendance Date",
      "Status",
      "Remarks",
    ];

    const rows = data.attendance.map((record) => [
      record.studentId,
      record.attendanceDate,
      record.status,
      record.remarks,
    ]);

    downloadCSV("My_Attendance.csv", headers, rows);
  };

  const downloadAcademicReportCSV = () => {
    const student = data.students[0] || {};

    const present = data.attendance.filter(
      (record) => record.status === "PRESENT"
    ).length;

    const absent = data.attendance.filter(
      (record) => record.status === "ABSENT"
    ).length;

    const leave = data.attendance.filter(
      (record) => record.status === "LEAVE"
    ).length;

    const totalCounted = present + absent;

    const percentage =
      attendancePercentage !== null
        ? Number(attendancePercentage).toFixed(2)
        : totalCounted > 0
          ? ((present / totalCounted) * 100).toFixed(2)
          : "0.00";

    const headers = [
      "Section",
      "Student Name",
      "Student ID",
      "Email",
      "Department",
      "Age",
      "Subject",
      "Semester",
      "Marks Obtained",
      "Maximum Marks",
      "Marks Percentage",
      "Grade",
      "Attendance Date",
      "Attendance Status",
      "Attendance Remarks",
      "Present Count",
      "Absent Count",
      "Leave Count",
      "Attendance Percentage",
    ];

    const rows = [];

    if (data.marks.length === 0 && data.attendance.length === 0) {
      rows.push([
        "Student Summary",
        student.name || "",
        student.id || user.studentId || "",
        student.email || user.email || "",
        student.department || "",
        student.age || "",
        "", "", "", "", "", "", "", "", "",
        present, absent, leave, percentage,
      ]);
    }

    data.marks.forEach((mark) => {
      const markPercentage =
        Number(mark.maximumMarks) > 0
          ? (
              (Number(mark.marksObtained) /
                Number(mark.maximumMarks)) *
              100
            ).toFixed(2)
          : "0.00";

      rows.push([
        "Marks",
        student.name || "",
        student.id || user.studentId || "",
        student.email || user.email || "",
        student.department || "",
        student.age || "",
        mark.subject,
        mark.semester,
        mark.marksObtained,
        mark.maximumMarks,
        markPercentage,
        mark.grade || "",
        "", "", "",
        present, absent, leave, percentage,
      ]);
    });

    data.attendance.forEach((record) => {
      rows.push([
        "Attendance",
        student.name || "",
        student.id || user.studentId || "",
        student.email || user.email || "",
        student.department || "",
        student.age || "",
        "", "", "", "", "", "",
        record.attendanceDate,
        record.status,
        record.remarks,
        present, absent, leave, percentage,
      ]);
    });

    downloadCSV("My_Academic_Report.csv", headers, rows);
  };

  // ------------------------------------------------
  // TABLE / SEARCH
  // ------------------------------------------------

  const currentRows = data[activePage] || [];

  const filteredRows = currentRows.filter((item) => {
    const searchValue = search.toLowerCase();

    return Object.values(item || {}).some((value) =>
      String(value ?? "").toLowerCase().includes(searchValue)
    );
  });

  const renderCell = (key, value) => {
    if (value === null || value === undefined || value === "") {
      return "-";
    }

    if (key.toLowerCase().includes("date")) {
      return formatDate(value);
    }

    if (typeof value === "object") {
      return JSON.stringify(value);
    }

    return String(value);
  };

  // ------------------------------------------------
  // DASHBOARD DATA
  // ------------------------------------------------

  const chartData = useMemo(() => [
    { name: "Students", total: data.students.length },
    { name: "Courses", total: data.courses.length },
    { name: "Marks", total: data.marks.length },
    { name: "Attendance", total: data.attendance.length },
  ], [data]);

  const dashboardCards = isStudent
    ? [
        {
          title: "My Profile",
          value: data.students.length,
          icon: UserRound,
          color: "bg-violet-100 text-violet-700",
        },
        {
          title: "My Marks Records",
          value: data.marks.length,
          icon: ClipboardList,
          color: "bg-blue-100 text-blue-700",
        },
        {
          title: "My Attendance Records",
          value: data.attendance.length,
          icon: CalendarCheck,
          color: "bg-green-100 text-green-700",
        },
      ]
    : [
        {
          title: "Total Students",
          value: data.students.length,
          icon: Users,
          color: "bg-violet-100 text-violet-700",
        },
        {
          title: "Total Courses",
          value: data.courses.length,
          icon: BookOpen,
          color: "bg-blue-100 text-blue-700",
        },
        {
          title: "Marks Records",
          value: data.marks.length,
          icon: ClipboardList,
          color: "bg-green-100 text-green-700",
        },
        {
          title: "Attendance Records",
          value: data.attendance.length,
          icon: CalendarCheck,
          color: "bg-orange-100 text-orange-700",
        },
      ];

  // ------------------------------------------------
  // LOGIN SCREEN
  // ------------------------------------------------

  if (!user) {
    return <Login onLogin={handleLogin} />;
  }

  // ------------------------------------------------
  // MAIN UI
  // ------------------------------------------------

  return (
    <div className="min-h-screen bg-gray-50 flex">

      {/* SIDEBAR */}

      <aside
        className={`fixed lg:static inset-y-0 left-0 z-40 w-64 bg-white border-r border-gray-200 transform transition-transform duration-200 ${
          sidebarOpen
            ? "translate-x-0"
            : "-translate-x-full lg:translate-x-0"
        }`}
      >
        <div className="h-20 px-6 flex items-center gap-3 border-b border-gray-100">
          <div className="bg-violet-600 p-2 rounded-xl text-white">
            <GraduationCap size={25} />
          </div>

          <div>
            <h1 className="text-xl font-bold text-gray-900">
              EduManage
            </h1>
            <p className="text-xs text-gray-500">
              Management System
            </p>
          </div>
        </div>

        <div className="p-4">
          <p className="text-xs font-bold uppercase tracking-wider text-gray-400 px-3 mb-3">
            Main Menu
          </p>

          <nav className="space-y-1">
            {visibleNavigation.map((item) => {
              const Icon = item.icon;
              const active = activePage === item.id;

              return (
                <button
                  key={item.id}
                  onClick={() => {
                    setActivePage(item.id);
                    setSearch("");
                    setSidebarOpen(false);
                    setSuccessMessage("");
                  }}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition ${
                    active
                      ? "bg-violet-600 text-white shadow-md shadow-violet-200"
                      : "text-gray-600 hover:bg-violet-50 hover:text-violet-700"
                  }`}
                >
                  <Icon size={19} />
                  {item.label}
                </button>
              );
            })}
          </nav>
        </div>

        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-100">
          <div className="flex items-center gap-3 mb-4 px-2">
            <div className="w-10 h-10 rounded-full bg-violet-100 text-violet-700 flex items-center justify-center">
              <UserRound size={20} />
            </div>

            <div className="min-w-0">
              <p className="text-sm font-semibold text-gray-800 truncate">
                {user.email}
              </p>
              <p className="text-xs text-violet-600 font-medium">
                {role}
              </p>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-3 text-red-600 hover:bg-red-50 rounded-xl text-sm font-semibold"
          >
            <LogOut size={19} />
            Logout
          </button>
        </div>
      </aside>

      {/* MOBILE OVERLAY */}

      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/30 z-30 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* MAIN CONTENT */}

      <main className="flex-1 min-w-0">

        {/* TOPBAR */}

        <header className="h-20 bg-white border-b border-gray-200 px-5 lg:px-8 flex items-center justify-between sticky top-0 z-20">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden p-2 rounded-lg hover:bg-gray-100"
            >
              <Menu size={22} />
            </button>

            <div>
              <h2 className="text-xl font-bold text-gray-900">
                {activePage === "dashboard"
                  ? "Dashboard"
                  : MODULES[activePage]?.title}
              </h2>
              <p className="text-xs text-gray-500">
                Welcome back, {user.email}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <span className="hidden sm:flex items-center gap-2 bg-violet-50 text-violet-700 px-3 py-2 rounded-lg text-xs font-bold">
              <ShieldCheck size={15} />
              {role}
            </span>

            <button
              onClick={loadData}
              disabled={loading}
              title="Refresh data"
              className="p-2 border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50"
            >
              <RefreshCw
                size={18}
                className={loading ? "animate-spin" : ""}
              />
            </button>
          </div>
        </header>

        {/* PAGE CONTENT */}

        <div className="p-5 lg:p-8">

          {errors.general && (
            <div className="mb-5 bg-red-50 border border-red-200 text-red-700 p-4 rounded-xl flex gap-3">
              <AlertCircle size={20} />
              <span>{errors.general}</span>
            </div>
          )}

          {successMessage && (
            <div className="mb-5 bg-green-50 border border-green-200 text-green-800 p-4 rounded-xl flex gap-3 items-start">
              <CheckCircle size={20} />
              <span>{successMessage}</span>
            </div>
          )}

          {loading && (
            <div className="mb-5 text-sm text-violet-700 flex items-center gap-2">
              <RefreshCw size={16} className="animate-spin" />
              Loading data...
            </div>
          )}

          {/* DASHBOARD */}

          {activePage === "dashboard" && (
            <div className="space-y-7">

              <div>
                <h3 className="text-2xl font-bold text-gray-900">
                  {isStudent ? "My Dashboard" : "Overview"}
                </h3>
                <p className="text-gray-500 mt-1">
                  {isStudent
                    ? "View your academic information."
                    : "Here is your system overview."}
                </p>
              </div>

              <div
                className={`grid grid-cols-1 sm:grid-cols-2 ${
                  isStudent ? "xl:grid-cols-3" : "xl:grid-cols-4"
                } gap-5`}
              >
                {dashboardCards.map((card) => (
                  <StatCard
                    key={card.title}
                    title={card.title}
                    value={card.value}
                    icon={card.icon}
                    color={card.color}
                  />
                ))}
              </div>

              {isStudent && (
                <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-5">
                  <h3 className="font-bold text-gray-900">
                    My Attendance Percentage
                  </h3>
                  <p className="text-3xl font-bold text-violet-700 mt-3">
                    {attendancePercentage === null
                      ? "Not available"
                      : `${Number(attendancePercentage).toFixed(2)}%`}
                  </p>
                  <p className="text-xs text-gray-500 mt-2">
                    Calculated by the Attendance Service. Leave records are excluded.
                  </p>
                </div>
              )}

              {!isStudent && (
                <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-5">
                  <div className="mb-5">
                    <h3 className="text-lg font-bold text-gray-900">
                      Module Overview
                    </h3>
                    <p className="text-sm text-gray-500">
                      Total records across modules
                    </p>
                  </div>

                  <div className="h-72">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={chartData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis allowDecimals={false} />
                        <Tooltip />
                        <Bar
                          dataKey="total"
                          fill="#7c3aed"
                          radius={[6, 6, 0, 0]}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              )}

              {isStudent && data.students[0] && (
                <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-5">
                    My Profile
                  </h3>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                    <div>
                      <p className="text-sm text-gray-500">Name</p>
                      <p className="font-semibold text-gray-900">
                        {getDisplayValue(data.students[0], ["name", "fullName"])}
                      </p>
                    </div>

                    <div>
                      <p className="text-sm text-gray-500">Email</p>
                      <p className="font-semibold text-gray-900">
                        {getDisplayValue(data.students[0], ["email"])}
                      </p>
                    </div>

                    <div>
                      <p className="text-sm text-gray-500">Department</p>
                      <p className="font-semibold text-gray-900">
                        {getDisplayValue(data.students[0], ["department"])}
                      </p>
                    </div>

                    <div>
                      <p className="text-sm text-gray-500">Age</p>
                      <p className="font-semibold text-gray-900">
                        {getDisplayValue(data.students[0], ["age"])}
                      </p>
                    </div>
                  </div>
                </div>
              )}

              <div>
                <h3 className="text-lg font-bold text-gray-900 mb-4">
                  Quick Access
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
                  {visibleNavigation
                    .filter((item) => item.id !== "dashboard")
                    .map((item) => {
                      const Icon = item.icon;

                      return (
                        <button
                          key={item.id}
                          onClick={() => setActivePage(item.id)}
                          className="bg-white border border-gray-100 shadow-sm rounded-2xl p-5 text-left hover:border-violet-300 hover:shadow-md transition"
                        >
                          <div className="w-11 h-11 bg-violet-100 text-violet-700 rounded-xl flex items-center justify-center mb-4">
                            <Icon size={22} />
                          </div>
                          <h4 className="font-bold text-gray-900">
                            {item.label}
                          </h4>
                          <p className="text-sm text-gray-500 mt-1">
                            View {item.label.toLowerCase()}
                          </p>
                        </button>
                      );
                    })}
                </div>
              </div>

              <AITest />
            </div>
          )}

          {/* MODULE PAGES */}

          {MODULES[activePage] && (
            <div className="space-y-6">

              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h3 className="text-2xl font-bold text-gray-900">
                    {currentModule.title}
                  </h3>
                  <p className="text-sm text-gray-500 mt-1">
                    {isStudent
                      ? `View your ${currentModule.title.toLowerCase()} records`
                      : `Manage ${currentModule.title.toLowerCase()} records`}
                  </p>
                </div>

                {canAdd && (
                  <button
                    onClick={openAddModal}
                    className="flex items-center justify-center gap-2 bg-violet-600 hover:bg-violet-700 text-white px-5 py-3 rounded-xl font-semibold"
                  >
                    <Plus size={19} />
                    Add {currentModule.singular}
                  </button>
                )}
              </div>

              {/* STUDENT CSV DOWNLOADS */}

              {isStudent &&
                ["marks", "attendance"].includes(activePage) && (
                  <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-5">
                    <h4 className="font-bold text-gray-900 mb-3">
                      Download My Reports
                    </h4>

                    <div className="flex flex-wrap gap-3">
                      {activePage === "marks" && (
                        <button
                          onClick={downloadMarksCSV}
                          disabled={data.marks.length === 0}
                          className="flex items-center gap-2 px-4 py-3 bg-violet-600 text-white rounded-xl hover:bg-violet-700 disabled:opacity-50"
                        >
                          <Download size={17} />
                          My Marks.csv
                        </button>
                      )}

                      {activePage === "attendance" && (
                        <button
                          onClick={downloadAttendanceCSV}
                          disabled={data.attendance.length === 0}
                          className="flex items-center gap-2 px-4 py-3 bg-violet-600 text-white rounded-xl hover:bg-violet-700 disabled:opacity-50"
                        >
                          <Download size={17} />
                          My Attendance.csv
                        </button>
                      )}

                      <button
                        onClick={downloadAcademicReportCSV}
                        className="flex items-center gap-2 px-4 py-3 bg-gray-800 text-white rounded-xl hover:bg-gray-900"
                      >
                        <Download size={17} />
                        My Academic Report.csv
                      </button>
                    </div>
                  </div>
                )}

              {/* SEARCH */}

              <div className="bg-white border border-gray-100 rounded-2xl p-4 shadow-sm">
                <div className="relative">
                  <Search
                    size={19}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                  />
                  <input
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder={`Search ${currentModule.title.toLowerCase()}...`}
                    className="w-full border border-gray-200 rounded-xl py-3 pl-10 pr-4 outline-none focus:ring-2 focus:ring-violet-500"
                  />
                </div>
              </div>

              {/* MODULE ERROR */}

              {errors[activePage] && (
                <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-xl">
                  <div className="flex items-center gap-2 font-semibold">
                    <AlertCircle size={18} />
                    Unable to load {currentModule.title}
                  </div>
                  <p className="text-sm mt-1">
                    {errors[activePage]}
                  </p>
                </div>
              )}

              {/* TABLE */}

              <div className="bg-white border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
                <div className="p-5 border-b border-gray-100 flex justify-between items-center">
                  <div>
                    <h4 className="font-bold text-gray-900">
                      {currentModule.title} List
                    </h4>
                    <p className="text-xs text-gray-500 mt-1">
                      {filteredRows.length} record(s) found
                    </p>
                  </div>
                </div>

                {filteredRows.length === 0 ? (
                  <div className="p-12 text-center">
                    <div className="w-14 h-14 bg-violet-50 text-violet-500 rounded-full flex items-center justify-center mx-auto mb-4">
                      {React.createElement(currentModule.icon, { size: 25 })}
                    </div>
                    <h4 className="font-semibold text-gray-800">
                      No records found
                    </h4>
                    <p className="text-sm text-gray-500 mt-1">
                      {errors[activePage]
                        ? "Check the API error above."
                        : "There is no data available in this module."}
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm text-left">
                      <thead className="bg-gray-50 text-gray-600">
                        <tr>
                          {Object.keys(filteredRows[0]).map((key) => (
                            <th
                              key={key}
                              className="px-5 py-4 font-semibold whitespace-nowrap"
                            >
                              {key.replace(/([A-Z])/g, " $1")}
                            </th>
                          ))}

                          {(canEdit || canDelete) && (
                            <th className="px-5 py-4 font-semibold">
                              Actions
                            </th>
                          )}
                        </tr>
                      </thead>

                      <tbody className="divide-y divide-gray-100">
                        {filteredRows.map((item, index) => (
                          <tr
                            key={getId(item) ?? index}
                            className="hover:bg-violet-50/40"
                          >
                            {Object.keys(filteredRows[0]).map((key) => (
                              <td
                                key={key}
                                className="px-5 py-4 text-gray-700 whitespace-nowrap"
                              >
                                {renderCell(key, item[key])}
                              </td>
                            ))}

                            {(canEdit || canDelete) && (
                              <td className="px-5 py-4">
                                <div className="flex items-center gap-2">
                                  {canEdit && (
                                    <button
                                      onClick={() => openEditModal(item)}
                                      title="Edit"
                                      className="p-2 rounded-lg text-blue-600 hover:bg-blue-50"
                                    >
                                      <Pencil size={17} />
                                    </button>
                                  )}

                                  {canDelete && (
                                    <button
                                      onClick={() => handleDelete(item)}
                                      title="Delete"
                                      className="p-2 rounded-lg text-red-600 hover:bg-red-50"
                                    >
                                      <Trash2 size={17} />
                                    </button>
                                  )}
                                </div>
                              </td>
                            )}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </main>

      {/* ADD / EDIT MODAL */}

      {modalOpen && currentModule && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">

            <div className="flex items-center justify-between p-6 border-b border-gray-100">
              <div>
                <h3 className="text-xl font-bold text-gray-900">
                  {editingItem ? "Edit" : "Add"} {currentModule.singular}
                </h3>
                <p className="text-sm text-gray-500 mt-1">
                  Enter the required details below.
                </p>
              </div>

              <button
                onClick={closeModal}
                className="p-2 rounded-lg hover:bg-gray-100"
              >
                <X size={21} />
              </button>
            </div>

            <form onSubmit={handleSave} className="p-6 space-y-5">
              {getFormFields().map((field) => (
                <div key={field.name}>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">
                    {field.label}
                  </label>

                  {field.type === "select" ? (
                    <select
                      required
                      value={formData[field.name] ?? ""}
                      onChange={(e) =>
                        setFormData((previous) => ({
                          ...previous,
                          [field.name]: e.target.value,
                        }))
                      }
                      className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-violet-500"
                    >
                      <option value="">
                        Select {field.label}
                      </option>
                      {field.options.map((option) => (
                        <option key={option} value={option}>
                          {option}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <input
                      type={field.type}
                      required
                      min={field.type === "number" ? "0" : undefined}
                      step={
                        ["marksObtained", "maximumMarks"].includes(field.name)
                          ? "0.01"
                          : "1"
                      }
                      value={formData[field.name] ?? ""}
                      onChange={(e) =>
                        setFormData((previous) => ({
                          ...previous,
                          [field.name]:
                            field.type === "number"
                              ? e.target.value === ""
                                ? ""
                                : Number(e.target.value)
                              : e.target.value,
                        }))
                      }
                      placeholder={`Enter ${field.label.toLowerCase()}`}
                      className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-violet-500"
                    />
                  )}
                </div>
              ))}

              {actionError && (
                <div className="bg-red-50 text-red-700 border border-red-200 rounded-xl p-3 text-sm">
                  {actionError}
                </div>
              )}

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={closeModal}
                  className="px-5 py-3 rounded-xl border border-gray-300 text-gray-700 font-semibold hover:bg-gray-50"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={saving}
                  className="px-5 py-3 rounded-xl bg-violet-600 text-white font-semibold hover:bg-violet-700 disabled:opacity-60"
                >
                  {saving
                    ? "Saving..."
                    : editingItem
                      ? "Update"
                      : "Save"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
