import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";

type Dose = "FIRST" | "SECOND" | "BOOSTER";

type Vaccine = {
  id: string;
  patientId: string;
  name: string;
  manufacturer: string | null;
  lot: string | null;
  applicationDate: string;
  dose: Dose;
  proof: string | null;
  observations: string | null;
};

const DOSE_LABEL: Record<Dose, string> = {
  FIRST: "1ª dose",
  SECOND: "2ª dose",
  BOOSTER: "Reforço",
};

function formatDate(iso: string): string {
  const [year, month, day] = iso.split("-");
  return `${day}/${month}/${year}`;
}

const EMPTY_FORM = {
  name: "",
  manufacturer: "",
  lot: "",
  applicationDate: "",
  dose: "FIRST" as Dose,
  observations: "",
};

export function VaccinesPage() {
  const { user, logout } = useAuth();
  const [vaccines, setVaccines] = useState<Vaccine[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  function loadVaccines() {
    if (!user?.id) return;
    setLoading(true);
    setError(null);
    api
      .get<Vaccine[]>(`/vaccines/pacientes/${user.id}/historico`)
      .then((res) => setVaccines(res.data))
      .catch(() => setError("Erro ao carregar histórico de vacinação. Tente novamente."))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadVaccines();
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
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!user?.id) return;

    if (!form.name.trim() || !form.applicationDate || !form.dose) {
      setFormError("Preencha nome, data de aplicação e dose.");
      return;
    }

    setSubmitting(true);
    setFormError(null);
    try {
      await api.post("/vaccines", {
        patientId: user.id,
        name: form.name,
        manufacturer: form.manufacturer || null,
        lot: form.lot || null,
        applicationDate: form.applicationDate,
        dose: form.dose,
        observations: form.observations || null,
      });
      closeModal();
      loadVaccines();
    } catch {
      setFormError("Erro ao salvar vacina. Tente novamente.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen bg-gray-100">
      {/* Sidebar */}
      <aside className="w-52 bg-gray-900 flex flex-col flex-shrink-0">
        <div className="flex items-center gap-2 px-5 py-5">
          <svg className="w-6 h-6 text-purple-400" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M4.318 6.318a4.5 4.5 0 016.364 0L12 7.636l1.318-1.318a4.5 4.5 0 116.364 6.364L12 20.364l-7.682-7.682a4.5 4.5 0 010-6.364z" />
          </svg>
          <span className="text-white font-semibold text-sm">Health Wallet</span>
        </div>

        <nav className="flex-1 px-3 py-2 space-y-1">
          <NavLink
            to="/anamnese"
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition ${
                isActive ? "bg-purple-600 text-white" : "text-gray-300 hover:bg-gray-800"
              }`
            }
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            Anamnese
          </NavLink>
          <NavLink
            to="/vacinas"
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition ${
                isActive ? "bg-purple-600 text-white" : "text-gray-300 hover:bg-gray-800"
              }`
            }
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
            </svg>
            Vacinas
          </NavLink>
        </nav>

        <div className="px-4 py-4 border-t border-gray-700 flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-purple-600 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
            {user?.name?.slice(0, 2).toUpperCase() ?? "??"}
          </div>
          <div className="overflow-hidden flex-1">
            <p className="text-white text-xs font-medium truncate">{user?.name}</p>
            <p className="text-gray-400 text-xs truncate">{user?.email}</p>
          </div>
          <button
            type="button"
            onClick={logout}
            title="Sair"
            className="text-gray-400 hover:text-white transition flex-shrink-0"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h6a2 2 0 012 2v1" />
            </svg>
          </button>
        </div>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="bg-white border-b border-gray-200 px-8 py-5 flex items-center justify-between">
          <h1 className="text-xl font-semibold text-gray-800">Histórico de Vacinação</h1>
          <button
            type="button"
            onClick={openModal}
            className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white text-sm font-semibold rounded-lg transition"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            Adicionar vacina
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-8 py-6">
          {error && (
            <div className="mb-5 bg-red-50 border border-red-300 text-red-700 rounded-lg px-4 py-3 text-sm">
              {error}
            </div>
          )}

          {loading ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center text-sm text-gray-500">
              Carregando histórico...
            </div>
          ) : vaccines.length === 0 && !error ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
              <p className="text-sm text-gray-500">Nenhuma vacina cadastrada ainda.</p>
            </div>
          ) : vaccines.length > 0 ? (
            <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200 text-left">
                    <th className="px-5 py-3 font-semibold text-gray-600">Vacina</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Data</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Dose</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Fabricante</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Lote</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Comprovante</th>
                  </tr>
                </thead>
                <tbody>
                  {vaccines.map((v) => (
                    <tr key={v.id} className="border-b border-gray-100 last:border-0">
                      <td className="px-5 py-4 text-gray-800">{v.name}</td>
                      <td className="px-5 py-4 text-gray-600">{formatDate(v.applicationDate)}</td>
                      <td className="px-5 py-4 text-gray-600">{DOSE_LABEL[v.dose]}</td>
                      <td className="px-5 py-4 text-gray-600">{v.manufacturer ?? "—"}</td>
                      <td className="px-5 py-4 text-gray-600">{v.lot ?? "—"}</td>
                      <td className="px-5 py-4">
                        {v.proof ? (
                          <a
                            href={v.proof}
                            target="_blank"
                            rel="noreferrer"
                            className="text-purple-600 hover:text-purple-700 hover:underline"
                          >
                            Ver comprovante
                          </a>
                        ) : (
                          <span className="text-gray-400">—</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </div>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-lg w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-800">Adicionar vacina</h2>
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

              <div>
                <label className="block text-sm text-gray-600 mb-1">Vacina *</label>
                <input
                  type="text"
                  name="name"
                  value={form.name}
                  onChange={handleFormChange}
                  placeholder="Ex: COVID-19"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Data de aplicação *</label>
                  <input
                    type="date"
                    name="applicationDate"
                    value={form.applicationDate}
                    onChange={handleFormChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Dose *</label>
                  <select
                    name="dose"
                    value={form.dose}
                    onChange={handleFormChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition bg-white"
                  >
                    <option value="FIRST">1ª dose</option>
                    <option value="SECOND">2ª dose</option>
                    <option value="BOOSTER">Reforço</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Fabricante</label>
                  <input
                    type="text"
                    name="manufacturer"
                    value={form.manufacturer}
                    onChange={handleFormChange}
                    placeholder="Ex: Pfizer"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Lote</label>
                  <input
                    type="text"
                    name="lot"
                    value={form.lot}
                    onChange={handleFormChange}
                    placeholder="Ex: ABC123"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Observações</label>
                <textarea
                  name="observations"
                  value={form.observations}
                  onChange={handleFormChange}
                  rows={3}
                  placeholder="Sem reações, etc."
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
