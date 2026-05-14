import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Sidebar } from "../components/Sidebar";
import { useAuth } from "../context/AuthContext";
import { api } from "../services/api";

type Vaccine = {
  id: string;
  name: string;
  applicationDate: string;
};

type Activity = {
  label: string;
  date: string;
};

function formatDate(iso: string): string {
  const [year, month, day] = iso.split("T")[0].split("-");
  return `${day}/${month}/${year}`;
}

export function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [anamnesisExists, setAnamnesisExists] = useState<boolean | null>(null);
  const [lastVaccine, setLastVaccine] = useState<Vaccine | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user?.id) return;

    async function fetchData() {
      setLoading(true);
      const acts: Activity[] = [];

      // Anamnese
      try {
        await api.get(`/anamnesis/${user!.id}`);
        setAnamnesisExists(true);
        acts.push({ label: "Anamnese registrada", date: new Date().toLocaleDateString("pt-BR") });
      } catch {
        setAnamnesisExists(false);
      }

      // Vacinas
      try {
        const { data } = await api.get(`/vaccines/pacientes/${user!.id}/historico`);
        if (data.length > 0) {
          const sorted = [...data].sort(
            (a: Vaccine, b: Vaccine) =>
              new Date(b.applicationDate).getTime() - new Date(a.applicationDate).getTime()
          );
          setLastVaccine(sorted[0]);
          sorted.slice(0, 3).forEach((v: Vaccine) => {
            acts.push({ label: `Vacina ${v.name} registrada`, date: formatDate(v.applicationDate) });
          });
        }
      } catch {
        setLastVaccine(null);
      }

      acts.sort((a, b) => {
        const toMs = (d: string) => {
          const [dd, mm, yy] = d.split("/").map(Number);
          return new Date(yy, mm - 1, dd).getTime();
        };
        return toMs(b.date) - toMs(a.date);
      });

      setActivities(acts);
      setLoading(false);
    }

    fetchData();
  }, [user?.id]);

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <div className="bg-white border-b border-gray-200 px-8 py-5">
          <h1 className="text-xl font-semibold text-gray-800">Dashboard</h1>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto px-8 py-6 space-y-6">

          {/* Cards 2x2 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">

            {/* Última Consulta */}
            <div className="bg-white rounded-xl border border-gray-200 p-5 flex items-start gap-4">
              <div className="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <div className="flex-1">
                <p className="text-xs text-gray-400 mb-1">Última Consulta</p>
                <p className="text-sm text-gray-400 italic">Sem consultas registradas</p>
              </div>
            </div>

            {/* Última Vacina */}
            <div
              className="bg-white rounded-xl border border-gray-200 p-5 flex items-start gap-4 cursor-pointer hover:border-purple-300 transition"
              onClick={() => navigate("/vacinas")}
            >
              <div className="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24" strokeLinecap="round" strokeLinejoin="round">
                  <path d="m18 2 4 4" />
                  <path d="m17 7 3-3" />
                  <path d="M19 9 8.7 19.3c-1 1-2.5 1-3.4 0l-.6-.6c-1-1-1-2.5 0-3.4L15 5Z" />
                  <path d="m9 11 4 4" />
                </svg>
              </div>
              <div className="flex-1">
                <p className="text-xs text-gray-400 mb-1">Última Vacina</p>
                {loading ? (
                  <p className="text-sm text-gray-400">Carregando...</p>
                ) : lastVaccine ? (
                  <>
                    <p className="text-base font-semibold text-gray-800">{lastVaccine.name}</p>
                    <p className="text-sm text-gray-500">{formatDate(lastVaccine.applicationDate)}</p>
                  </>
                ) : (
                  <p className="text-sm text-gray-400 italic">Nenhuma vacina registrada</p>
                )}
              </div>
            </div>

            {/* Anamnese */}
            <div className="bg-white rounded-xl border border-gray-200 p-5 flex items-start gap-4">
              <div className="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
              <div className="flex-1">
                <p className="text-xs text-gray-400 mb-1">Anamnese</p>
                {anamnesisExists === null ? (
                  <p className="text-sm text-gray-400">Carregando...</p>
                ) : anamnesisExists ? (
                  <div className="flex items-center gap-3 mt-1">
                    <span className="px-2.5 py-0.5 bg-green-100 text-green-700 text-xs font-semibold rounded-full">
                      Completa
                    </span>
                    <button
                      onClick={() => navigate("/anamnese")}
                      className="text-xs text-purple-600 hover:text-purple-800 font-medium transition"
                    >
                      Editar
                    </button>
                  </div>
                ) : (
                  <div className="flex items-center gap-3 mt-1">
                    <span className="px-2.5 py-0.5 bg-yellow-100 text-yellow-700 text-xs font-semibold rounded-full">
                      Pendente
                    </span>
                    <button
                      onClick={() => navigate("/anamnese")}
                      className="text-xs text-purple-600 hover:text-purple-800 font-medium transition"
                    >
                      Preencher
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Relatório de Saúde */}
            <div className="bg-white rounded-xl border border-gray-200 p-5 flex items-start gap-4">
              <div className="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <div>
                <p className="text-xs text-gray-400 mb-2">Relatório de Saúde</p>
                <button
                  disabled
                  title="Em breve"
                  className="px-4 py-1.5 bg-purple-600 text-white text-xs font-semibold rounded-lg opacity-50 cursor-not-allowed"
                >
                  Gerar relatório
                </button>
              </div>
            </div>
          </div>

          {/* Atividade Recente */}
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h2 className="text-base font-semibold text-gray-800 mb-4">Atividade Recente</h2>
            {loading ? (
              <p className="text-sm text-gray-400">Carregando...</p>
            ) : activities.length === 0 ? (
              <p className="text-sm text-gray-400">Nenhuma atividade registrada ainda.</p>
            ) : (
              <div className="divide-y divide-gray-100">
                {activities.map((item, i) => (
                  <div key={i} className="flex items-center justify-between py-3">
                    <div className="flex items-center gap-3">
                      <div className="w-7 h-7 rounded-full bg-purple-50 flex items-center justify-center flex-shrink-0">
                        <svg className="w-3.5 h-3.5 text-purple-500" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                        </svg>
                      </div>
                      <span className="text-sm text-gray-700">{item.label}</span>
                    </div>
                    <span className="text-xs text-gray-400">{item.date}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

        </div>
      </div>
    </div>
  );
}
