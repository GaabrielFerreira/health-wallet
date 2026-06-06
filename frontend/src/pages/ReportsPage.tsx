import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { Sidebar } from "../components/Sidebar";

type ReportType = "FULL" | "ANAMNESIS" | "VACCINES" | "APPOINTMENTS";
type ReportStatus = "PROCESSING" | "COMPLETED" | "ERROR";

type Report = {
  id: string;
  patientId: string;
  type: ReportType;
  status: ReportStatus;
  generatedAt: string | null;
  content: string | null;
};

const TYPE_LABELS: Record<ReportType, string> = {
  FULL: "Completo",
  ANAMNESIS: "Anamnese",
  VACCINES: "Vacinas",
  APPOINTMENTS: "Consultas",
};

const STATUS_LABELS: Record<ReportStatus, string> = {
  PROCESSING: "Processando",
  COMPLETED: "Concluído",
  ERROR: "Erro",
};

const STATUS_STYLES: Record<ReportStatus, string> = {
  PROCESSING: "bg-yellow-100 text-yellow-700",
  COMPLETED: "bg-green-100 text-green-700",
  ERROR: "bg-red-100 text-red-700",
};

const EMPTY_FILTERS = {
  type: "",
  startDate: "",
  endDate: "",
};

function formatDateTime(iso: string | null): string {
  if (!iso) return "—";
  const d = new Date(iso);
  return d.toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function ReportsPage() {
  const { user } = useAuth();
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [type, setType] = useState<ReportType>("FULL");
  const [generating, setGenerating] = useState(false);
  const [viewing, setViewing] = useState<Report | null>(null);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);

  function buildQuery() {
    const params = new URLSearchParams();
    if (filters.type) params.set("type", filters.type);
    if (filters.startDate) params.set("startDate", filters.startDate);
    if (filters.endDate) params.set("endDate", filters.endDate);
    const qs = params.toString();
    return qs ? `?${qs}` : "";
  }

  function loadReports() {
    if (!user?.id) return;
    setLoading(true);
    api
      .get<Report[]>(`/reports/pacientes/${user.id}${buildQuery()}`)
      .then((res) => setReports(res.data))
      .catch(() => toast.error("Erro ao carregar relatórios. Tente novamente."))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadReports();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  async function handleGenerate() {
    if (!user?.id) return;
    setGenerating(true);
    try {
      const res = await api.post<Report>("/reports", {
        patientId: user.id,
        type,
      });
      toast.success("Relatório gerado com sucesso!");
      setViewing(res.data);
      loadReports();
    } catch {
      toast.error("Erro ao gerar relatório. Tente novamente.");
    } finally {
      setGenerating(false);
    }
  }

  async function downloadPdf(report: Report) {
    setDownloadingId(report.id);
    try {
      const res = await api.get(`/reports/${report.id}/pdf`, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(
        new Blob([res.data], { type: "application/pdf" })
      );
      const link = document.createElement("a");
      link.href = url;
      link.download = `relatorio-${report.type.toLowerCase()}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error("Erro ao baixar o PDF. Tente novamente.");
    } finally {
      setDownloadingId(null);
    }
  }

  function handleFilterChange(
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  }

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="bg-white border-b border-gray-200 px-8 py-5 flex items-center justify-between gap-4 flex-wrap">
          <h1 className="text-xl font-semibold text-gray-800">Relatórios de Saúde</h1>
          <div className="flex items-center gap-3">
            <select
              value={type}
              onChange={(e) => setType(e.target.value as ReportType)}
              className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
            >
              {(Object.keys(TYPE_LABELS) as ReportType[]).map((t) => (
                <option key={t} value={t}>
                  {TYPE_LABELS[t]}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={handleGenerate}
              disabled={generating}
              className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white text-sm font-semibold rounded-lg transition"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
              </svg>
              {generating ? "Gerando..." : "Gerar relatório"}
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-8 py-6">
          {/* Filtros */}
          <div className="bg-white rounded-xl border border-gray-200 px-5 py-4 mb-5 flex flex-wrap gap-4 items-end">
            <div>
              <label className="block text-xs text-gray-500 mb-1">Tipo</label>
              <select
                name="type"
                value={filters.type}
                onChange={handleFilterChange}
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition w-44"
              >
                <option value="">Todos</option>
                {(Object.keys(TYPE_LABELS) as ReportType[]).map((t) => (
                  <option key={t} value={t}>
                    {TYPE_LABELS[t]}
                  </option>
                ))}
              </select>
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
              onClick={loadReports}
              className="px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white text-sm font-semibold rounded-lg transition"
            >
              Filtrar
            </button>
            <button
              type="button"
              onClick={() => setFilters(EMPTY_FILTERS)}
              className="px-4 py-2 text-sm text-gray-500 hover:text-gray-700 transition"
            >
              Limpar
            </button>
          </div>

          {loading ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center text-sm text-gray-500">
              Carregando relatórios...
            </div>
          ) : reports.length === 0 ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
              <p className="text-sm text-gray-500">
                Nenhum relatório gerado ainda. Selecione um tipo e clique em "Gerar relatório".
              </p>
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200 text-left">
                    <th className="px-5 py-3 font-semibold text-gray-600">Tipo</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Gerado em</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Status</th>
                    <th className="px-5 py-3 font-semibold text-gray-600 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {reports.map((r) => (
                    <tr key={r.id} className="border-b border-gray-100 last:border-0 hover:bg-purple-50 transition">
                      <td className="px-5 py-4 text-gray-800">{TYPE_LABELS[r.type]}</td>
                      <td className="px-5 py-4 text-gray-600 whitespace-nowrap">{formatDateTime(r.generatedAt)}</td>
                      <td className="px-5 py-4">
                        <span className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_STYLES[r.status]}`}>
                          {STATUS_LABELS[r.status]}
                        </span>
                      </td>
                      <td className="px-5 py-4">
                        <div className="flex items-center justify-end gap-3">
                          <button
                            type="button"
                            onClick={() => setViewing(r)}
                            className="text-purple-600 hover:text-purple-800 font-medium transition"
                          >
                            Ver
                          </button>
                          <button
                            type="button"
                            onClick={() => downloadPdf(r)}
                            disabled={downloadingId === r.id}
                            className="text-gray-600 hover:text-gray-900 font-medium transition disabled:text-gray-400"
                          >
                            {downloadingId === r.id ? "Baixando..." : "PDF"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {viewing && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4" onClick={() => setViewing(null)}>
          <div className="bg-white rounded-2xl shadow-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-gray-800">Relatório {TYPE_LABELS[viewing.type]}</h2>
                <p className="text-sm text-gray-400 mt-0.5">{formatDateTime(viewing.generatedAt)}</p>
              </div>
              <button
                type="button"
                onClick={() => setViewing(null)}
                className="text-gray-400 hover:text-gray-600 transition"
                aria-label="Fechar"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="px-6 py-5">
              {viewing.content ? (
                <pre className="text-xs text-gray-700 whitespace-pre-wrap font-mono bg-gray-50 border border-gray-200 rounded-lg p-4 overflow-x-auto">
                  {viewing.content}
                </pre>
              ) : (
                <p className="text-sm text-gray-400 text-center py-4">Relatório sem conteúdo.</p>
              )}
            </div>

            <div className="px-6 py-4 border-t border-gray-200 flex justify-end">
              <button
                type="button"
                onClick={() => downloadPdf(viewing)}
                disabled={downloadingId === viewing.id}
                className="px-5 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white text-sm font-semibold rounded-lg transition"
              >
                {downloadingId === viewing.id ? "Baixando..." : "Baixar PDF"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
