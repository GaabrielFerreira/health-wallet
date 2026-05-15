import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { Sidebar } from "../components/Sidebar";

const BLOOD_TYPES = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];
const ACTIVITY_LEVELS = ["Sedentário", "Leve", "Moderado", "Intenso"];
const ALCOHOL_LEVELS = ["Nunca", "Ocasional", "Frequente"];

type FormFields = {
  bloodType: string;
  weight: string;
  height: string;
  allergies: string;
  chronicDiseases: string;
  previousSurgeries: string;
  medications: string;
  familyHistory: string;
  observations: string;
  smoker: boolean;
  physicalActivity: string;
  alcoholConsumption: string;
};

type FormErrors = Partial<Record<keyof FormFields, string>>;

function validate(form: FormFields): FormErrors {
  const errors: FormErrors = {};
  if (!form.allergies.trim()) errors.allergies = "Campo obrigatório.";
  if (!form.chronicDiseases.trim()) errors.chronicDiseases = "Campo obrigatório.";
  if (!form.medications.trim()) errors.medications = "Campo obrigatório.";
  if (!form.bloodType) errors.bloodType = "Selecione o tipo sanguíneo.";
  return errors;
}

const INITIAL_FORM: FormFields = {
  bloodType: "",
  weight: "",
  height: "",
  allergies: "",
  chronicDiseases: "",
  previousSurgeries: "",
  medications: "",
  familyHistory: "",
  observations: "",
  smoker: false,
  physicalActivity: "Moderado",
  alcoholConsumption: "Ocasional",
};

export function AnamnesisPage() {
  const { user } = useAuth();
  const [form, setForm] = useState<FormFields>(INITIAL_FORM);

  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);
  const [saving, setSaving] = useState(false);
  const [exists, setExists] = useState(false);
  const [lastUpdate, setLastUpdate] = useState<string | null>(null);

  // Carregar anamnese existente
  useEffect(() => {
    if (!user?.id) return;
    async function load() {
      try {
        const { data } = await api.get(`/anamnesis/${user!.id}`);
        setExists(true);
        setForm({
          ...INITIAL_FORM,
          bloodType: data.bloodType ?? "",
          allergies: data.allergies ?? "",
          chronicDiseases: data.chronicDiseases ?? "",
          medications: data.medications ?? "",
          familyHistory: data.familyHistory ?? "",
          observations: data.observations ?? "",
          weight: data.weight != null ? String(data.weight) : "",
          height: data.height != null ? String(data.height) : "",
          previousSurgeries: data.previousSurgeries ?? "",
          smoker: data.smoker ?? false,
          physicalActivity: data.physicalActivity ?? "Moderado",
          alcoholConsumption: data.alcoholConsumption ?? "Ocasional",
        });
        if (data.updatedAt) setLastUpdate(data.updatedAt);
      } catch {
        setExists(false);
      }
    }
    load();
  }, [user?.id]);

  function handleChange(
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) {
    const { name, value, type } = e.target;
    const updated = {
      ...form,
      [name]: type === "checkbox" ? (e.target as HTMLInputElement).checked : value,
    };
    setForm(updated);
    if (submitted) setErrors(validate(updated));
  }

  function handleCancel() {
    setForm(INITIAL_FORM);
    setErrors({});
    setSubmitted(false);
  }

  async function handleSubmit(e: React.SyntheticEvent) {
    e.preventDefault();
    if (!user?.id) return;

    setSubmitted(true);
    const validationErrors = validate(form);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setSaving(true);
    const payload = {
      bloodType: form.bloodType,
      allergies: form.allergies,
      chronicDiseases: form.chronicDiseases,
      medications: form.medications,
      familyHistory: form.familyHistory,
      observations: form.observations,
      weight: form.weight ? Number(form.weight) : null,
      height: form.height ? Number(form.height) : null,
      previousSurgeries: form.previousSurgeries,
      smoker: form.smoker,
      physicalActivity: form.physicalActivity,
      alcoholConsumption: form.alcoholConsumption,
    };

    try {
      const wasUpdate = exists;
      if (exists) {
        const { data } = await api.put(`/anamnesis/${user.id}`, payload);
        if (data?.updatedAt) setLastUpdate(data.updatedAt);
      } else {
        const { data } = await api.post("/anamnesis", { patientId: user.id, ...payload });
        setExists(true);
        if (data?.updatedAt) setLastUpdate(data.updatedAt);
      }
      toast.success(wasUpdate ? "Anamnese atualizada com sucesso!" : "Anamnese salva com sucesso!");
    } catch {
      toast.error("Erro ao salvar anamnese. Tente novamente.");
    } finally {
      setSaving(false);
    }
  }

  const inputClass = (field: keyof FormErrors) =>
    `w-full border rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 transition bg-white ${
      errors[field]
        ? "border-red-400 focus:ring-red-300"
        : "border-gray-300 focus:ring-purple-400 focus:border-purple-400"
    }`;

  const textareaClass = (field: keyof FormErrors) =>
    `w-full border rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 transition resize-none bg-white ${
      errors[field]
        ? "border-red-400 focus:ring-red-300"
        : "border-gray-300 focus:ring-purple-400 focus:border-purple-400"
    }`;

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />

      {/* Main */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <div className="bg-white border-b border-gray-200 px-8 py-5">
          <h1 className="text-xl font-semibold text-gray-800">Anamnese</h1>
          <p className="text-xs text-gray-400 mt-0.5">
            {exists && lastUpdate
              ? `Última atualização: ${new Date(lastUpdate).toLocaleDateString("pt-BR")}`
              : "Preencha os dados abaixo para registrar sua anamnese"}
          </p>
        </div>

        {/* Content */}
        <form onSubmit={handleSubmit} noValidate className="flex-1 overflow-y-auto px-8 py-6 space-y-5">

          {/* Row 1: Dados Gerais + Histórico Médico */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">

            {/* Dados Gerais */}
            <div className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
              <h2 className="text-base font-semibold text-gray-700 border-b border-gray-100 pb-2">
                Dados Gerais
              </h2>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Peso (kg)</label>
                  <input
                    type="number"
                    name="weight"
                    value={form.weight}
                    onChange={handleChange}
                    placeholder="78"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition bg-white"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 mb-1">Altura (cm)</label>
                  <input
                    type="number"
                    name="height"
                    value={form.height}
                    onChange={handleChange}
                    placeholder="175"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition bg-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Tipo Sanguíneo</label>
                <select
                  name="bloodType"
                  value={form.bloodType}
                  onChange={handleChange}
                  className={inputClass("bloodType")}
                >
                  <option value="" disabled>Selecione</option>
                  {BLOOD_TYPES.map(bt => (
                    <option key={bt} value={bt}>{bt}</option>
                  ))}
                </select>
                {errors.bloodType && <p className="text-red-500 text-xs mt-1">{errors.bloodType}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Alergias</label>
                <textarea
                  name="allergies"
                  value={form.allergies}
                  onChange={handleChange}
                  rows={4}
                  placeholder="Ex: Dipirona, Penicilina"
                  className={textareaClass("allergies")}
                />
                {errors.allergies && <p className="text-red-500 text-xs mt-1">{errors.allergies}</p>}
              </div>
            </div>

            {/* Histórico Médico */}
            <div className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
              <h2 className="text-base font-semibold text-gray-700 border-b border-gray-100 pb-2">
                Histórico Médico
              </h2>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Doenças crônicas</label>
                <textarea
                  name="chronicDiseases"
                  value={form.chronicDiseases}
                  onChange={handleChange}
                  rows={3}
                  placeholder="Ex: Hipertensão arterial leve"
                  className={textareaClass("chronicDiseases")}
                />
                {errors.chronicDiseases && <p className="text-red-500 text-xs mt-1">{errors.chronicDiseases}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Cirurgias anteriores</label>
                <textarea
                  name="previousSurgeries"
                  value={form.previousSurgeries}
                  onChange={handleChange}
                  rows={3}
                  placeholder="Ex: Apendicectomia (2015)"
                  className={textareaClass("previousSurgeries")}
                />
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Medicamentos em uso</label>
                <textarea
                  name="medications"
                  value={form.medications}
                  onChange={handleChange}
                  rows={3}
                  placeholder="Ex: Losartana 50mg (1x ao dia)"
                  className={textareaClass("medications")}
                />
                {errors.medications && <p className="text-red-500 text-xs mt-1">{errors.medications}</p>}
              </div>
            </div>
          </div>

          {/* Histórico Familiar */}
          <div className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
            <h2 className="text-base font-semibold text-gray-700 border-b border-gray-100 pb-2">
              Histórico Familiar
            </h2>
            <textarea
              name="familyHistory"
              value={form.familyHistory}
              onChange={handleChange}
              rows={3}
              placeholder="Ex: Pai com diabetes tipo 2, mãe com histórico de hipertensão"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition resize-none bg-white"
            />
          </div>

          {/* Hábitos */}
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h2 className="text-base font-semibold text-gray-700 border-b border-gray-100 pb-2 mb-4">
              Hábitos
            </h2>
            <div className="flex flex-wrap items-center gap-8">
              {/* Fumante toggle */}
              <div className="flex items-center gap-3">
                <span className="text-sm text-gray-600">Fumante</span>
                <button
                  type="button"
                  onClick={() => setForm({ ...form, smoker: !form.smoker })}
                  className={`relative w-10 h-5 rounded-full transition-colors duration-200 ${
                    form.smoker ? "bg-purple-600" : "bg-gray-300"
                  }`}
                >
                  <span
                    className={`absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform duration-200 ${
                      form.smoker ? "translate-x-5" : "translate-x-0"
                    }`}
                  />
                </button>
              </div>

              {/* Atividade física */}
              <div className="flex items-center gap-3">
                <label className="text-sm text-gray-600 whitespace-nowrap">Atividade física</label>
                <select
                  name="physicalActivity"
                  value={form.physicalActivity}
                  onChange={handleChange}
                  className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition bg-white"
                >
                  {ACTIVITY_LEVELS.map(l => <option key={l} value={l}>{l}</option>)}
                </select>
              </div>

              {/* Consumo de álcool */}
              <div className="flex items-center gap-3">
                <label className="text-sm text-gray-600 whitespace-nowrap">Consumo de álcool</label>
                <select
                  name="alcoholConsumption"
                  value={form.alcoholConsumption}
                  onChange={handleChange}
                  className="border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition bg-white"
                >
                  {ALCOHOL_LEVELS.map(l => <option key={l} value={l}>{l}</option>)}
                </select>
              </div>
            </div>
          </div>

          {/* Observações */}
          <div className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
            <h2 className="text-base font-semibold text-gray-700 border-b border-gray-100 pb-2">
              Observações
            </h2>
            <textarea
              name="observations"
              value={form.observations}
              onChange={handleChange}
              rows={3}
              placeholder="Informações adicionais relevantes para o histórico de saúde"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition resize-none bg-white"
            />
          </div>

          {/* Spacer for fixed footer */}
          <div className="h-4" />
        </form>

        {/* Footer fixo */}
        <div className="bg-white border-t border-gray-200 px-8 py-4 flex justify-end gap-3">
          <button
            type="button"
            onClick={handleCancel}
            className="px-5 py-2 text-sm text-gray-600 hover:text-gray-800 transition"
          >
            Cancelar
          </button>
          <button
            type="submit"
            form=""
            onClick={handleSubmit}
            disabled={saving}
            className="px-6 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white text-sm font-semibold rounded-lg transition"
          >
            {saving
              ? "Salvando..."
              : exists
                ? "Salvar alterações"
                : "Criar anamnese"}
          </button>
        </div>
      </div>
    </div>
  );
}
