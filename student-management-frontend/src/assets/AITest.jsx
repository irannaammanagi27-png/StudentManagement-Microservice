import { useState } from "react";
import { Bot, Send } from "lucide-react";
import { aiAPI } from "../api";

function AITest() {
  const [message, setMessage] = useState("");
  const [reply, setReply] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChat = async (event) => {
    event.preventDefault();
    if (!message.trim()) return;

    try {
      setLoading(true);
      setReply("");
      setError("");

      const response = await aiAPI.chat(message);
      setReply(response.reply);
    } catch (error) {
      setError(error.message || "Unable to contact the AI assistant.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="bg-white border border-gray-100 rounded-2xl shadow-sm p-5">
      <div className="flex items-center gap-3 mb-4">
        <div className="bg-violet-100 text-violet-700 p-2 rounded-xl">
          <Bot size={22} />
        </div>
        <div>
          <h3 className="font-bold text-gray-900">AI Assistant</h3>
          <p className="text-sm text-gray-500">Ask a question to get started.</p>
        </div>
      </div>

      <form onSubmit={handleChat} className="flex flex-col sm:flex-row gap-3">
        <input
          value={message}
          onChange={(event) => setMessage(event.target.value)}
          placeholder="Ask me anything..."
          aria-label="Message for the AI assistant"
          className="flex-1 border border-gray-200 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-violet-500"
        />
        <button
          type="submit"
          disabled={loading || !message.trim()}
          className="flex items-center justify-center gap-2 bg-violet-600 hover:bg-violet-700 text-white px-5 py-3 rounded-xl font-semibold disabled:opacity-60"
        >
          <Send size={17} />
          {loading ? "Thinking..." : "Ask AI"}
        </button>
      </form>

      {error && (
        <p role="alert" className="mt-4 text-sm text-red-700">
          {error}
        </p>
      )}
      {reply && (
        <div className="mt-4 rounded-xl bg-violet-50 p-4 text-sm text-gray-800 whitespace-pre-wrap">
          {reply}
        </div>
      )}
    </section>
  );
}

export default AITest;