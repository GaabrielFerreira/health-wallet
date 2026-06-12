import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { Sidebar } from "../components/Sidebar";

type AccessStatus = "ACTIVE" | "EXPIRED" | "REVOKED";

type DoctorAccess = {
  id: string;
  patientId: string;
  dataTypes: string | null;
  grantedAt: string;
  expiresAt: string | null;
  status: AccessStatus;
};

type Anamnesis = {
  allergies: string | null;
  chronicDiseases: string | null;
  medications: string | null;
  bloodType: string | null;
  familyHistory: string | null;
  observations: string | null;
} | null;

type Vaccine = {
  id: string;
  name: string;
  applicationDate: string;
  dose: string;
  manufacturer: string | null;
};

type Appointment = {
  id: string;
  date: string;
  specialty: string;
  professional: string;
  summary: string | null;
};

type PatientData = {
  anamnesis: Anamnesis;
  vaccines: Vaccine[];
  appointments: Appointment[];
};

function formatDateTime(iso: string | null): string {
  if (!iso) return "Sem expiração";
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString("pt-BR");
}

export function DoctorPatientsPage() {
  const { user } = useAuth();
  const [accesses, setAccesses] = useState<DoctorAccess[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewing, setViewing] = useState<DoctorAccess | null>(null);
  const [patientData, setPatientData] = useState<PatientData | null>(null);
  const [loadingData, setLoadingData] = useState(false);

  function loadAccesses() {
    if (!user?.id) return;
    setLoading(true);
    api
      .get<DoctorAccess[]>(`/permissoes/medicos/${user.id}`)
      .then((res) => setAccesses(res.data))
      .catch(() => toast.error("Erro ao carregar pacientes. Tente novamente."))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadAccesses();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  async function openPatientData(access: DoctorAccess) {
    if (!user?.id) return;
    setViewing(access);
    setPatientData(null);
    setLoadingData(true);
    try {
      const res = await api.get<PatientData>(
        `/permissoes/medicos/${user.id}/pacientes/${access.patientId}/dados`
      );
      setPatientData(res.data);
    } catch {
      toast.error("Não foi possível carregar os dados do paciente.");
      setViewing(null);
    } finally {
      setLoadingData(false);
    }
  }

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="bg-white border-b border-gray-200 px-8 py-5">
          <h1 className="text-xl font-semibold text-gray-800">Meus Pacientes</h1>
          <p className="text-xs text-gray-500 mt-1">Pacientes que concederam acesso aos seus dados de saúde.</p>
        </div>

        <div className="flex-1 overflow-y-auto px-8 py-6">
          {loading ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center text-sm text-gray-500">
              Carregando pacientes...
            </div>
          ) : accesses.length === 0 ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center text-sm text-gray-500">
              Nenhum paciente concedeu acesso a você ainda.
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200 text-left">
                    <th className="px-5 py-3 font-semibold text-gray-600">Paciente</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Acesso desde</th>
                    <th className="px-5 py-3 font-semibold text-gray-600">Expira em</th>
                    <th className="px-5 py-3 font-semibold text-gray-600 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {accesses.map((a) => (
                    <tr key={a.id} className="border-b border-gray-100 last:border-0 hover:bg-purple-50 transition">
                      <td className="px-5 py-4 text-gray-800 font-mono text-xs">{a.patientId.slice(0, 8)}</td>
                      <td className="px-5 py-4 text-gray-600 whitespace-nowrap">{formatDateTime(a.grantedAt)}</td>
                      <td className="px-5 py-4 text-gray-600 whitespace-nowrap">{formatDateTime(a.expiresAt)}</td>
                      <td className="px-5 py-4 text-right">
                        <button
                          type="button"
                          onClick={() => openPatientData(a)}
                          className="text-purple-600 hover:text-purple-800 font-medium transition"
                        >
                          Ver dados
                        </button>
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
              <h2 className="text-lg font-semibold text-gray-800">Dados do paciente</h2>
              <button type="button" onClick={() => setViewing(null)} className="text-gray-400 hover:text-gray-600 transition" aria-label="Fechar">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="px-6 py-5 space-y-6">
              {loadingData ? (
                <p className="text-sm text-gray-500 text-center py-4">Carregando dados...</p>
              ) : patientData ? (
                <>
                  {/* Anamnese */}
                  <section>
                    <h3 className="text-sm font-semibold text-gray-800 mb-2">Anamnese</h3>
                    {patientData.anamnesis ? (
                      <div className="grid grid-cols-2 gap-3 text-sm">
                        <Field label="Alergias" value={patientData.anamnesis.allergies} />
                        <Field label="Doenças crônicas" value={patientData.anamnesis.chronicDiseases} />
                        <Field label="Medicações" value={patientData.anamnesis.medications} />
                        <Field label="Tipo sanguíneo" value={patientData.anamnesis.bloodType} />
                        <Field label="Histórico familiar" value={patientData.anamnesis.familyHistory} />
                        <Field label="Observações" value={patientData.anamnesis.observations} />
                      </div>
                    ) : (
                      <p className="text-sm text-gray-400">Sem anamnese registrada.</p>
                    )}
                  </section>

                  {/* Vacinas */}
                  <section>
                    <h3 className="text-sm font-semibold text-gray-800 mb-2">Vacinas ({patientData.vaccines.length})</h3>
                    {patientData.vaccines.length ? (
                      <ul className="space-y-1 text-sm text-gray-700">
                        {patientData.vaccines.map((v) => (
                          <li key={v.id} className="border-b border-gray-100 pb-1 last:border-0">
                            <span className="text-gray-800">{v.name}</span>
                            <span className="text-gray-400"> · {formatDate(v.applicationDate)} · {v.dose}{v.manufacturer ? ` · ${v.manufacturer}` : ""}</span>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-sm text-gray-400">Nenhuma vacina registrada.</p>
                    )}
                  </section>

                  {/* Consultas */}
                  <section>
                    <h3 className="text-sm font-semibold text-gray-800 mb-2">Consultas ({patientData.appointments.length})</h3>
                    {patientData.appointments.length ? (
                      <ul className="space-y-1 text-sm text-gray-700">
                        {patientData.appointments.map((c) => (
                          <li key={c.id} className="border-b border-gray-100 pb-1 last:border-0">
                            <span className="text-gray-800">{c.specialty}</span>
                            <span className="text-gray-400"> · {formatDateTime(c.date)} · {c.professional}</span>
                            {c.summary && <p className="text-xs text-gray-500 mt-0.5">{c.summary}</p>}
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-sm text-gray-400">Nenhuma consulta registrada.</p>
                    )}
                  </section>
                </>
              ) : null}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function Field({ label, value }: { label: string; value: string | null }) {
  return (
    <div>
      <p className="text-xs text-gray-400 mb-0.5">{label}</p>
      <p className="text-gray-700">{value || "—"}</p>
    </div>
  );
}
