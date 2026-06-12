import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { Sidebar } from "../components/Sidebar";

type AccessStatus = "ACTIVE" | "EXPIRED" | "REVOKED";

type DoctorAccess = {
  id: string;
  patientId: string;
  doctorId: string;
  doctorName: string;
  doctorEmail: string;
  dataTypes: string | null;
  grantedAt: string;
  expiresAt: string | null;
  revoked: boolean;
  status: AccessStatus;
};

type ShareLink = {
  token: string;
  expiresAt: string;
};

const STATUS_LABELS: Record<AccessStatus, string> = {
  ACTIVE: "Ativo",
  EXPIRED: "Expirado",
  REVOKED: "Revogado",
};

const STATUS_STYLES: Record<AccessStatus, string> = {
  ACTIVE: "bg-green-100 text-green-700",
  EXPIRED: "bg-yellow-100 text-yellow-700",
  REVOKED: "bg-red-100 text-red-700",
};

const VALIDITY_OPTIONS = [
  { label: "24 horas", value: 24 },
  { label: "48 horas", value: 48 },
  { label: "7 dias", value: 168 },
];

const DATA_TYPE_OPTIONS = ["Anamnese", "Vacinas", "Consultas"];

function formatDateTime(iso: string | null): string {
  if (!iso) return "Sem expiração";
  const d = new Date(iso);
  return d.toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function SharingPage() {
  const { user } = useAuth();
  const [accesses, setAccesses] = useState<DoctorAccess[]>([]);
  const [loading, setLoading] = useState(true);

  // link temporário
  const [expiresInHours, setExpiresInHours] = useState(24);
  const [generating, setGenerating] = useState(false);
  const [shareLink, setShareLink] = useState<ShareLink | null>(null);

  // conceder acesso
  const [isGrantOpen, setIsGrantOpen] = useState(false);
  const [grantForm, setGrantForm] = useState({ doctorEmail: "", expiresAt: "" });
  const [grantDataTypes, setGrantDataTypes] = useState<string[]>([]);
  const [granting, setGranting] = useState(false);

  // renovar
  const [renewTarget, setRenewTarget] = useState<DoctorAccess | null>(null);
  const [renewDate, setRenewDate] = useState("");
  const [renewing, setRenewing] = useState(false);

  function loadAccesses() {
    if (!user?.id) return;
    setLoading(true);
    api
      .get<DoctorAccess[]>(`/permissoes/pacientes/${user.id}`)
      .then((res) => setAccesses(res.data))
      .catch(() => toast.error("Erro ao carregar acessos. Tente novamente."))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadAccesses();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  async function handleGenerateLink() {
    if (!user?.id) return;
    setGenerating(true);
    try {
      const res = await api.post<ShareLink>("/share/generate", {
        patientId: user.id,
        expiresInHours,
      });
      setShareLink(res.data);
      toast.success("Link gerado com sucesso!");
    } catch {
      toast.error("Erro ao gerar o link. Tente novamente.");
    } finally {
      setGenerating(false);
    }
  }

  function shareUrl(token: string): string {
    return `${window.location.origin}/api/share/${token}`;
  }

  async function copyLink() {
    if (!shareLink) return;
    try {
      await navigator.clipboard.writeText(shareUrl(shareLink.token));
      toast.success("Link copiado!");
    } catch {
      toast.error("Não foi possível copiar. Copie manualmente.");
    }
  }

  function openGrant() {
    setGrantForm({ doctorEmail: "", expiresAt: "" });
    setGrantDataTypes([]);
    setIsGrantOpen(true);
  }

  function toggleDataType(type: string) {
    setGrantDataTypes((prev) =>
      prev.includes(type) ? prev.filter((t) => t !== type) : [...prev, type]
    );
  }

  async function handleGrant(e: React.FormEvent) {
    e.preventDefault();
    if (!user?.id) return;
    if (!grantForm.doctorEmail.trim()) {
      toast.error("Informe o email do médico.");
      return;
    }
    setGranting(true);
    try {
      const lookup = await api.get<{ id: string; name: string; email: string }>(
        "/permissoes/medico-por-email",
        { params: { email: grantForm.doctorEmail.trim() } }
      );
      await api.post("/permissoes", {
        patientId: user.id,
        doctorId: lookup.data.id,
        dataTypes: grantDataTypes.length ? grantDataTypes.join(", ") : null,
        expiresAt: grantForm.expiresAt ? `${grantForm.expiresAt}:00` : null,
      });
      toast.success(`Acesso concedido a ${lookup.data.name}.`);
      setIsGrantOpen(false);
      loadAccesses();
    } catch {
      toast.error("Erro ao conceder acesso. Verifique se o email é de um médico cadastrado.");
    } finally {
      setGranting(false);
    }
  }

  async function handleRevoke(access: DoctorAccess) {
    try {
      await api.delete(`/permissoes/${access.id}`);
      toast.success("Acesso revogado.");
      loadAccesses();
    } catch {
      toast.error("Erro ao revogar o acesso. Tente novamente.");
    }
  }

  function openRenew(access: DoctorAccess) {
    setRenewTarget(access);
    setRenewDate("");
  }

  async function handleRenew(e: React.FormEvent) {
    e.preventDefault();
    if (!renewTarget) return;
    if (!renewDate) {
      toast.error("Informe a nova data de expiração.");
      return;
    }
    setRenewing(true);
    try {
      await api.patch(`/permissoes/${renewTarget.id}/renovar`, {
        expiresAt: `${renewDate}:00`,
      });
      toast.success("Acesso renovado.");
      setRenewTarget(null);
      loadAccesses();
    } catch {
      toast.error("Erro ao renovar. A data deve ser no futuro.");
    } finally {
      setRenewing(false);
    }
  }

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="bg-white border-b border-gray-200 px-8 py-5">
          <h1 className="text-xl font-semibold text-gray-800">Compartilhamento de Dados</h1>
        </div>

        <div className="flex-1 overflow-y-auto px-8 py-6 space-y-6">
          {/* Link temporário */}
          <div className="bg-white rounded-xl border border-gray-200 px-6 py-5">
            <h2 className="text-sm font-semibold text-gray-800 mb-1">Link de acesso temporário</h2>
            <p className="text-xs text-gray-500 mb-4">
              Gere um link com prazo de validade para um médico acessar seus dados consolidados sem precisar de cadastro.
            </p>

            <div className="flex flex-wrap items-end gap-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">Validade</label>
                <select
                  value={expiresInHours}
                  onChange={(e) => setExpiresInHours(Number(e.target.value))}
                  className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                >
                  {VALIDITY_OPTIONS.map((o) => (
                    <option key={o.value} value={o.value}>{o.label}</option>
                  ))}
                </select>
              </div>
              <button
                type="button"
                onClick={handleGenerateLink}
                disabled={generating}
                className="px-4 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white text-sm font-semibold rounded-lg transition"
              >
                {generating ? "Gerando..." : "Gerar link"}
              </button>
            </div>

            {shareLink && (
              <div className="mt-4 bg-gray-50 border border-gray-200 rounded-lg px-4 py-3">
                <div className="flex items-center gap-2">
                  <input
                    type="text"
                    readOnly
                    value={shareUrl(shareLink.token)}
                    className="flex-1 bg-white border border-gray-300 rounded-lg px-3 py-2 text-sm text-gray-700 outline-none"
                  />
                  <button
                    type="button"
                    onClick={copyLink}
                    className="px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white text-sm font-semibold rounded-lg transition whitespace-nowrap"
                  >
                    Copiar
                  </button>
                </div>
                <p className="text-xs text-gray-500 mt-2">Expira em {formatDateTime(shareLink.expiresAt)}</p>
              </div>
            )}
          </div>

          {/* Acessos de médicos */}
          <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-sm font-semibold text-gray-800">Médicos com acesso</h2>
              <button
                type="button"
                onClick={openGrant}
                className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white text-sm font-semibold rounded-lg transition"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                </svg>
                Conceder acesso
              </button>
            </div>

            {loading ? (
              <div className="p-12 text-center text-sm text-gray-500">Carregando acessos...</div>
            ) : accesses.length === 0 ? (
              <div className="p-12 text-center text-sm text-gray-500">
                Nenhum médico tem acesso aos seus dados ainda.
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200 text-left">
                    <th className="px-5 py-3 font-semibold text-gray-600">Médico</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Concedido em</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Expira em</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Status</th>
                    <th className="px-5 py-3 font-semibold text-gray-600 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {accesses.map((a) => (
                    <tr key={a.id} className="border-b border-gray-100 last:border-0 hover:bg-purple-50 transition">
                      <td className="px-5 py-4">
                        <p className="text-gray-800">{a.doctorName}</p>
                        <p className="text-xs text-gray-400">{a.doctorEmail}</p>
                      </td>
                      <td className="px-5 py-4 text-gray-600 whitespace-nowrap">{formatDateTime(a.grantedAt)}</td>
                      <td className="px-5 py-4 text-gray-600 whitespace-nowrap">{formatDateTime(a.expiresAt)}</td>
                      <td className="px-5 py-4">
                        <span className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_STYLES[a.status]}`}>
                          {STATUS_LABELS[a.status]}
                        </span>
                      </td>
                      <td className="px-5 py-4">
                        <div className="flex items-center justify-end gap-3">
                          {a.status !== "REVOKED" && (
                            <button
                              type="button"
                              onClick={() => openRenew(a)}
                              className="text-purple-600 hover:text-purple-800 font-medium transition"
                            >
                              Renovar
                            </button>
                          )}
                          {a.status === "ACTIVE" && (
                            <button
                              type="button"
                              onClick={() => handleRevoke(a)}
                              className="text-red-600 hover:text-red-800 font-medium transition"
                            >
                              Revogar
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>

      {/* Modal conceder acesso */}
      {isGrantOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-lg w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-800">Conceder acesso a um médico</h2>
              <button type="button" onClick={() => setIsGrantOpen(false)} className="text-gray-400 hover:text-gray-600 transition" aria-label="Fechar">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleGrant} className="px-6 py-5 space-y-4">
              <div>
                <label className="block text-sm text-gray-600 mb-1">Email do médico *</label>
                <input
                  type="email"
                  value={grantForm.doctorEmail}
                  onChange={(e) => setGrantForm((p) => ({ ...p, doctorEmail: e.target.value }))}
                  placeholder="medico@exemplo.com"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                />
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Dados compartilhados</label>
                <div className="flex flex-wrap gap-3">
                  {DATA_TYPE_OPTIONS.map((type) => (
                    <label key={type} className="flex items-center gap-2 text-sm text-gray-700">
                      <input
                        type="checkbox"
                        checked={grantDataTypes.includes(type)}
                        onChange={() => toggleDataType(type)}
                        className="accent-purple-600"
                      />
                      {type}
                    </label>
                  ))}
                </div>
                <p className="text-xs text-gray-400 mt-1">Deixe em branco para compartilhar tudo.</p>
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Expira em (opcional)</label>
                <input
                  type="datetime-local"
                  value={grantForm.expiresAt}
                  onChange={(e) => setGrantForm((p) => ({ ...p, expiresAt: e.target.value }))}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setIsGrantOpen(false)} className="px-5 py-2 text-sm text-gray-600 hover:text-gray-800 transition">
                  Cancelar
                </button>
                <button type="submit" disabled={granting} className="px-6 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white text-sm font-semibold rounded-lg transition">
                  {granting ? "Concedendo..." : "Conceder"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal renovar */}
      {renewTarget && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-lg w-full max-w-md">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-800">Renovar acesso</h2>
              <button type="button" onClick={() => setRenewTarget(null)} className="text-gray-400 hover:text-gray-600 transition" aria-label="Fechar">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleRenew} className="px-6 py-5 space-y-4">
              <p className="text-sm text-gray-600">
                Renovando o acesso de <span className="font-medium text-gray-800">{renewTarget.doctorName}</span>.
              </p>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Nova data de expiração *</label>
                <input
                  type="datetime-local"
                  value={renewDate}
                  onChange={(e) => setRenewDate(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition"
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setRenewTarget(null)} className="px-5 py-2 text-sm text-gray-600 hover:text-gray-800 transition">
                  Cancelar
                </button>
                <button type="submit" disabled={renewing} className="px-6 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white text-sm font-semibold rounded-lg transition">
                  {renewing ? "Renovando..." : "Renovar"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
