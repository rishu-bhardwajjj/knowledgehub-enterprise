
import { useEffect, useState } from "react";
import {
  getDocuments,
  createDocument,
  deleteDocument,
  updateDocument,
} from "./api/document";
import "./App.css";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const EMPTY_FORM = {
  title: "",
  description: "",
  category: "",
  uploadedBy: "admin",
};

function App() {
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState("");
  const [formError, setFormError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [form, setForm] = useState({ ...EMPTY_FORM });

  // Document upload state
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState("");
  const [uploadError, setUploadError] = useState("");

  // AI chatbot state
  const [question, setQuestion] = useState("");
  const [chatMessages, setChatMessages] = useState([]);
  const [asking, setAsking] = useState(false);
  const [chatError, setChatError] = useState("");

  async function loadDocuments() {
    try {
      setLoading(true);
      setError("");

      const data = await getDocuments();
      setDocuments(data);
    } catch (err) {
      setError(err.message || "Unable to load documents");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDocuments();
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((previous) => ({ ...previous, [name]: value }));
  }

  function openCreateForm() {
    setEditingId(null);
    setForm({ ...EMPTY_FORM });
    setFormError("");
    setShowForm(true);
  }

  function openEditForm(document) {
    setEditingId(document.id);
    setForm({
      title: document.title || "",
      description: document.description || "",
      category: document.category || "",
      uploadedBy: document.uploadedBy || "",
    });
    setFormError("");
    setShowForm(true);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function closeForm() {
    setShowForm(false);
    setEditingId(null);
    setForm({ ...EMPTY_FORM });
    setFormError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setFormError("");

    try {
      if (editingId) {
        await updateDocument(editingId, form);
      } else {
        await createDocument(form);
      }

      closeForm();
      await loadDocuments();
    } catch (err) {
      setFormError(
        err.message ||
          (editingId
            ? "Unable to update document"
            : "Unable to create document")
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id) {
    const confirmed = window.confirm(
      "Are you sure you want to delete this document?"
    );

    if (!confirmed) return;

    setDeletingId(id);
    setError("");

    try {
      await deleteDocument(id);
      await loadDocuments();
    } catch (err) {
      setError(err.message || "Unable to delete document");
    } finally {
      setDeletingId("");
    }
  }

  // Upload a PDF, DOCX, or TXT file to the backend.
  async function handleFileUpload(event) {
    event.preventDefault();

    if (!selectedFile) {
      setUploadError("Please select a file first.");
      return;
    }

    const allowedExtensions = /\.(pdf|docx|txt)$/i;

    if (!allowedExtensions.test(selectedFile.name)) {
      setUploadError("Only PDF, DOCX, and TXT files are supported.");
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    setUploading(true);
    setUploadError("");
    setUploadMessage("");

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/documents/upload`,
        {
          method: "POST",
          body: formData,
        }
      );

      const responseText = await response.text();

      if (!response.ok) {
        throw new Error(
          responseText || `Upload failed (${response.status})`
        );
      }

      let result;

      try {
        result = JSON.parse(responseText);
      } catch {
        result = responseText;
      }

      const message =
        typeof result === "string"
          ? result
          : result.message ||
            result.title ||
            JSON.stringify(result);

      setUploadMessage(`Upload successful: ${message}`);
      setSelectedFile(null);
      event.target.reset();

      await loadDocuments();
    } catch (err) {
      setUploadError(err.message || "Unable to upload document.");
    } finally {
      setUploading(false);
    }
  }

  // Ask the RAG chatbot a question about uploaded documents.
  async function handleAskQuestion(event) {
    event.preventDefault();

    const currentQuestion = question.trim();

    if (!currentQuestion || asking) return;

    setAsking(true);
    setChatError("");
    setQuestion("");

    setChatMessages((previous) => [
      ...previous,
      {
        role: "user",
        content: currentQuestion,
      },
    ]);

    try {
      const response = await fetch(`${API_BASE_URL}/api/ai/ask`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          question: currentQuestion,
        }),
      });

      const responseText = await response.text();

      if (!response.ok) {
        throw new Error(
          responseText || `Request failed (${response.status})`
        );
      }

      let result;

      try {
        result = JSON.parse(responseText);
      } catch {
        result = responseText;
      }

      const answerText =
        typeof result === "string"
          ? result
          : result.answer ||
            result.response ||
            result.message ||
            JSON.stringify(result);

      setChatMessages((previous) => [
        ...previous,
        {
          role: "assistant",
          content: answerText,
        },
      ]);
    } catch (err) {
      setChatError(err.message || "Unable to get an AI response.");
    } finally {
      setAsking(false);
    }
  }

  const categories = [
    "All",
    ...new Set(documents.map((doc) => doc.category).filter(Boolean)),
  ];

  const query = searchTerm.toLowerCase().trim();

  const filteredDocuments = documents.filter((doc) => {
    const matchesSearch =
      doc.title?.toLowerCase().includes(query) ||
      doc.description?.toLowerCase().includes(query);

    const matchesCategory =
      selectedCategory === "All" || doc.category === selectedCategory;

    return matchesSearch && matchesCategory;
  });

  return (
    <main className="app">
      <header className="app-header">
        <div>
          <p className="eyebrow">KNOWLEDGE MANAGEMENT</p>
          <h1>KnowledgeHub</h1>
          <p className="subtitle">
            Your team's knowledge, organized in one place.
          </p>
        </div>

        <div className="header-actions">
          <button
            onClick={loadDocuments}
            disabled={loading || saving}
          >
            {loading ? "Loading..." : "Refresh"}
          </button>

          <button
            onClick={showForm ? closeForm : openCreateForm}
            disabled={saving}
          >
            {showForm ? "Cancel" : "+ New Document"}
          </button>
        </div>
      </header>

      {/* Selected document details */}
      {selectedDocument && (
        <section className="details-panel">
          <div className="details-header">
            <h2>Document Details</h2>
            <button
              className="cancel-button"
              onClick={() => setSelectedDocument(null)}
            >
              Close
            </button>
          </div>

          <h3>{selectedDocument.title}</h3>
          <p>
            {selectedDocument.description || "No description provided."}
          </p>

          <dl className="details-list">
            <dt>Category</dt>
            <dd>{selectedDocument.category}</dd>

            <dt>Uploaded by</dt>
            <dd>{selectedDocument.uploadedBy}</dd>

            <dt>Created</dt>
            <dd>
              {selectedDocument.createdAt
                ? new Date(selectedDocument.createdAt).toLocaleString()
                : "Not available"}
            </dd>

            <dt>Document ID</dt>
            <dd className="document-id">{selectedDocument.id}</dd>
          </dl>
        </section>
      )}

      {/* Create or edit document metadata */}
      {showForm && (
        <section className="form-section">
          <h2>{editingId ? "Edit document" : "Add a document"}</h2>

          <form className="document-form" onSubmit={handleSubmit}>
            <label>
              Title
              <input
                name="title"
                value={form.title}
                onChange={handleChange}
                maxLength={200}
                required
                placeholder="e.g. Employee Handbook"
              />
            </label>

            <label>
              Description
              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                maxLength={2000}
                rows={3}
                placeholder="What is this document about?"
              />
            </label>

            <label>
              Category
              <input
                name="category"
                value={form.category}
                onChange={handleChange}
                maxLength={100}
                required
                placeholder="e.g. HR, IT, Engineering"
              />
            </label>

            <label>
              Uploaded by
              <input
                name="uploadedBy"
                value={form.uploadedBy}
                onChange={handleChange}
                maxLength={100}
                required
              />
            </label>

            {formError && <p className="form-error">{formError}</p>}

            <div className="form-actions">
              <button type="submit" disabled={saving}>
                {saving
                  ? "Saving..."
                  : editingId
                    ? "Update Document"
                    : "Save Document"}
              </button>

              <button
                type="button"
                className="cancel-button"
                onClick={closeForm}
                disabled={saving}
              >
                Cancel
              </button>
            </div>
          </form>
        </section>
      )}

      {/* File upload */}
      <section className="form-section">
        <h2>Upload a Document</h2>
        <p>
          Upload a PDF, DOCX, or TXT file to make its content searchable
          by KnowledgeHub AI.
        </p>

        <form className="document-form" onSubmit={handleFileUpload}>
          <label>
            Choose a file
            <input
              type="file"
              accept=".pdf,.docx,.txt"
              onChange={(event) => {
                setSelectedFile(event.target.files?.[0] || null);
                setUploadError("");
                setUploadMessage("");
              }}
              required
            />
          </label>

          {selectedFile && (
            <p>
              Selected: {selectedFile.name} (
              {(selectedFile.size / 1024).toFixed(1)} KB)
            </p>
          )}

          {uploadError && (
            <p className="form-error">{uploadError}</p>
          )}

          {uploadMessage && <p role="status">{uploadMessage}</p>}

          <button type="submit" disabled={uploading || !selectedFile}>
            {uploading ? "Uploading..." : "Upload Document"}
          </button>
        </form>
      </section>

      {/* AI chatbot */}
      <section className="form-section">
        <h2>KnowledgeHub AI</h2>
        <p>
          Ask questions about the documents you have uploaded.
        </p>

        <div className="chat-messages" aria-live="polite">
          {chatMessages.length === 0 && (
            <p className="empty-state">
              Your conversation will appear here. Try asking a question
              about an uploaded document.
            </p>
          )}

          {chatMessages.map((message, index) => (
            <article
              className={`chat-message ${
                message.role === "user" ? "user-message" : "assistant-message"
              }`}
              key={`${message.role}-${index}`}
            >
              <strong>
                {message.role === "user" ? "You" : "KnowledgeHub AI"}
              </strong>
              <p style={{ whiteSpace: "pre-wrap" }}>{message.content}</p>
            </article>
          ))}

          {asking && (
            <p className="status">KnowledgeHub AI is thinking...</p>
          )}
        </div>

        {chatError && <p className="form-error">{chatError}</p>}

        <form className="document-form" onSubmit={handleAskQuestion}>
          <label>
            Your question
            <textarea
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="e.g. Summarize the main points of my document."
              rows={3}
              maxLength={4000}
              required
            />
          </label>

          <button
            type="submit"
            disabled={asking || !question.trim()}
          >
            {asking ? "Thinking..." : "Ask AI"}
          </button>
        </form>
      </section>

      {/* Existing document list, search and category filters */}
      <section className="documents-section">
        <div className="section-heading">
          <h2>All Documents</h2>
          <span>
            {filteredDocuments.length} of {documents.length} documents
          </span>
        </div>

        <div className="document-filters">
          <input
            type="search"
            placeholder="Search by title or description..."
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            aria-label="Search documents"
          />

          <select
            value={selectedCategory}
            onChange={(event) => setSelectedCategory(event.target.value)}
            aria-label="Filter by category"
          >
            {categories.map((category) => (
              <option key={category} value={category}>
                {category === "All" ? "All categories" : category}
              </option>
            ))}
          </select>
        </div>

        {loading && <p className="status">Loading documents...</p>}

        {!loading && error && (
          <div className="error-message">
            <p>{error}</p>
            <button onClick={loadDocuments}>Try again</button>
          </div>
        )}

        {!loading && !error && filteredDocuments.length === 0 && (
          <p className="empty-state">
            {documents.length === 0
              ? "No documents found. Add one to get started."
              : "No documents match your search or category."}
          </p>
        )}

        {!loading && !error && filteredDocuments.length > 0 && (
          <div className="document-grid">
            {filteredDocuments.map((document) => (
              <article className="document-card" key={document.id}>
                <div className="document-card-top">
                  <span className="category">{document.category}</span>
                  <span className="document-icon">▤</span>
                </div>

                <h3>{document.title}</h3>

                <p className="document-description">
                  {document.description || "No description provided."}
                </p>

                <div className="document-meta">
                  <span>Uploaded by {document.uploadedBy}</span>
                  <span>
                    {document.createdAt
                      ? new Date(document.createdAt).toLocaleDateString()
                      : ""}
                  </span>
                </div>

                <div className="card-actions">
                  <button
                    className="view-button"
                    onClick={() => setSelectedDocument(document)}
                  >
                    View Details
                  </button>

                  <button
                    className="edit-button"
                    onClick={() => openEditForm(document)}
                    disabled={saving || deletingId !== ""}
                  >
                    Edit
                  </button>

                  <button
                    className="delete-button"
                    onClick={() => handleDelete(document.id)}
                    disabled={deletingId !== "" || saving}
                  >
                    {deletingId === document.id ? "Deleting..." : "Delete"}
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

export default App;
