import { useEffect, useState } from "react";
import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { Sidebar } from "../components/Sidebar";

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
  const { user } = useAuth();
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
      <Sidebar />

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
