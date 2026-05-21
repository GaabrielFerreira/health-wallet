import { useEffect, useState } from "react";
import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { Sidebar } from "../components/Sidebar";

type Appointment = {
  id: string;
  patientId: string;
  date: string;
  specialty: string;
  professional: string;
  clinic: string | null;
  summary: string | null;
  prescription: string | null;
  medicalObservation: string | null;
};

const EMPTY_FORM = {
  date: "",
  specialty: "",
  professional: "",
  clinic: "",
  summary: "",
  prescription: "",
  medicalObservation: "",
};

const EMPTY_FILTERS = {
  specialty: "",
  professional: "",
  startDate: "",
  endDate: "",
};

function formatDateTime(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function ConsultasPage() {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [selected, setSelected] = useState<Appointment | null>(null);

  function buildQuery() {
    const params = new URLSearchParams();
    if (filters.specialty) params.set("specialty", filters.specialty);
    if (filters.professional) params.set("professional", filters.professional);
    if (filters.startDate) params.set("startDate", filters.startDate);
    if (filters.endDate) params.set("endDate", filters.endDate);
    const qs = params.toString();
    return qs ? `?${qs}` : "";
  }

  function loadAppointments() {
    if (!user?.id) return;
    setLoading(true);
    setError(null);
    api
      .get<Appointment[]>(`/appointments/pacientes/${user.id}${buildQuery()}`)
      .then((res) => setAppointments(res.data))
      .catch(() => setError("Erro ao carregar consultas. Tente novamente."))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadAppointments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  function openModal() {
    setForm(EMPTY_FORM);
    setFormError(null);
    setIsModalOpen(true);
  }

  function closeModal() {
    setIsModalOpen(false);
  }

  function handleFormChange(
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function handleFilterChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!user?.id) return;

    if (!form.specialty.trim() || !form.professional.trim() || !form.date) {
      setFormError("Preencha especialidade, profissional e data.");
      return;
    }

    setSubmitting(true);
    setFormError(null);
    try {
      await api.post<Appointment>("/appointments", {
        patientId: user.id,
        date: form.date.length === 16 ? `${form.date}:00` : form.date,
        specialty: form.specialty,
        professional: form.professional,
        clinic: form.clinic || null,
        summary: form.summary || null,
        prescription: form.prescription || null,
        medicalObservation: form.medicalObservation || null,
      });
      closeModal();
      loadAppointments();
    } catch {
      setFormError("Erro ao salvar consulta. Tente novamente.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="bg-white border-b border-gray-200 px-8 py-5 flex items-center justify-between">
          <h1 className="text-xl font-semibold text-gray-800">Consultas Clínicas</h1>
          <button
            type="button"
            onClick={openModal}
            className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white text-sm font-semibold rounded-lg transition"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            Adicionar consulta
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-8 py-6">
          {error && (
            <div className="mb-5 bg-red-50 border border-red-300 text-red-700 rounded-lg px-4 py-3 text-sm">
              {error}
            </div>
          )}

          {/* Filtros */}
          <div className="bg-white rounded-xl border border-gray-200 px-5 py-4 mb-5 flex flex-wrap gap-4 items-end">
            <div>
              <label className="block text-xs text-gray-500 mb-1">Especialidade</label>
              <input
                type="text"
                name="specialty"
                value={filters.specialty}
                onChange={handleFilterChange}
                placeholder="Ex: Cardiologia"
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition w-48"
              />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">Profissional</label>
              <input
                type="text"
                name="professional"
                value={filters.professional}
                onChange={handleFilterChange}
                placeholder="Ex: Dr. Silva"
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition w-48"
              />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">De</label>
              <input
                type="date"
                name="startDate"
                value={filters.startDate}
                onChange={handleFilterChange}
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
              />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">Até</label>
              <input
                type="date"
                name="endDate"
                value={filters.endDate}
                onChange={handleFilterChange}
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
              />
            </div>
            <button
              type="button"
              onClick={loadAppointments}
              className="px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white text-sm font-semibold rounded-lg transition"
            >
              Filtrar
            </button>
            <button
              type="button"
              onClick={() => { setFilters(EMPTY_FILTERS); }}
              className="px-4 py-2 text-sm text-gray-500 hover:text-gray-700 transition"
            >
              Limpar
            </button>
          </div>

          {loading ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center text-sm text-gray-500">
              Carregando consultas...
            </div>
          ) : appointments.length === 0 && !error ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
              <p className="text-sm text-gray-500">Nenhuma consulta encontrada.</p>
            </div>
          ) : appointments.length > 0 ? (
            <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200 text-left">
                    <th className="px-5 py-3 font-semibold text-gray-600">Data</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Especialidade</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Profissional</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Clínica</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Resumo</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((a) => (
                    <tr
                      key={a.id}
                      onClick={() => setSelected(a)}
                      className="border-b border-gray-100 last:border-0 cursor-pointer hover:bg-purple-50 transition"
                    >
                      <td className="px-5 py-4 text-gray-600 whitespace-nowrap">{formatDateTime(a.date)}</td>
                      <td className="px-5 py-4 text-gray-800">{a.specialty}</td>
                      <td className="px-5 py-4 text-gray-600">{a.professional}</td>
                      <td className="px-5 py-4 text-gray-600">{a.clinic ?? "—"}</td>
                      <td className="px-5 py-4 text-gray-600 max-w-xs truncate">{a.summary ?? "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </div>
      </div>

      {selected && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4" onClick={() => setSelected(null)}>
          <div className="bg-white rounded-2xl shadow-lg w-full max-w-lg max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-gray-800">{selected.specialty}</h2>
                <p className="text-sm text-gray-400 mt-0.5">{formatDateTime(selected.date)}</p>
              </div>
              <button
                type="button"
                onClick={() => setSelected(null)}
                className="text-gray-400 hover:text-gray-600 transition"
                aria-label="Fechar"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="px-6 py-5 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-xs text-gray-400 mb-1">Profissional</p>
                  <p className="text-sm text-gray-800">{selected.professional}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Clínica</p>
                  <p className="text-sm text-gray-800">{selected.clinic ?? "—"}</p>
                </div>
              </div>

              {selected.summary && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">Resumo da consulta</p>
                  <p className="text-sm text-gray-700 whitespace-pre-wrap">{selected.summary}</p>
                </div>
              )}

              {selected.prescription && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">Prescrição</p>
                  <p className="text-sm text-gray-700 whitespace-pre-wrap">{selected.prescription}</p>
                </div>
              )}

              {selected.medicalObservation && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">Observações médicas</p>
                  <p className="text-sm text-gray-700 whitespace-pre-wrap">{selected.medicalObservation}</p>
                </div>
              )}

              {!selected.summary && !selected.prescription && !selected.medicalObservation && (
                <p className="text-sm text-gray-400 text-center py-2">Nenhuma informação adicional registrada.</p>
              )}
            </div>
          </div>
        </div>
      )}

      {isModalOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-lg w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-800">Adicionar consulta</h2>
              <button
                type="button"
                onClick={closeModal}
                className="text-gray-400 hover:text-gray-600 transition"
                aria-label="Fechar"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
              {formError && (
                <div className="bg-red-50 border border-red-300 text-red-700 rounded-lg px-3 py-2 text-sm">
                  {formError}
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Especialidade *</label>
                  <input
                    type="text"
                    name="specialty"
                    value={form.specialty}
                    onChange={handleFormChange}
                    placeholder="Ex: Cardiologia"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Data *</label>
                  <input
                    type="datetime-local"
                    name="date"
                    value={form.date}
                    onChange={handleFormChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Profissional *</label>
                  <input
                    type="text"
                    name="professional"
                    value={form.professional}
                    onChange={handleFormChange}
                    placeholder="Ex: Dr. Silva"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Clínica</label>
                  <input
                    type="text"
                    name="clinic"
                    value={form.clinic}
                    onChange={handleFormChange}
                    placeholder="Ex: Clínica Central"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Resumo da consulta</label>
                <textarea
                  name="summary"
                  value={form.summary}
                  onChange={handleFormChange}
                  rows={2}
                  placeholder="Principais pontos da consulta..."
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition resize-none"
                />
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Prescrição</label>
                <textarea
                  name="prescription"
                  value={form.prescription}
                  onChange={handleFormChange}
                  rows={2}
                  placeholder="Medicamentos prescritos..."
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition resize-none"
                />
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Observações médicas</label>
                <textarea
                  name="medicalObservation"
                  value={form.medicalObservation}
                  onChange={handleFormChange}
                  rows={2}
                  placeholder="Observações adicionais..."
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition resize-none"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={closeModal}
                  className="px-5 py-2 text-sm text-gray-600 hover:text-gray-800 transition"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-6 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white text-sm font-semibold rounded-lg transition"
                >
                  {submitting ? "Salvando..." : "Salvar"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
