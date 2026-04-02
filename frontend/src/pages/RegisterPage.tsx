import { useState } from "react";
import { useNavigate } from "react-router-dom";

function maskCPF(value: string): string {
  return value
    .replace(/\D/g, "")
    .slice(0, 11)
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

function isValidCPF(cpf: string): boolean {
  const digits = cpf.replace(/\D/g, "");
  if (digits.length !== 11) return false;
  if (/^(\d)\1{10}$/.test(digits)) return false;

  let sum = 0;
  for (let i = 0; i < 9; i++) sum += parseInt(digits[i]) * (10 - i);
  let remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  if (remainder !== parseInt(digits[9])) return false;

  sum = 0;
  for (let i = 0; i < 10; i++) sum += parseInt(digits[i]) * (11 - i);
  remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  return remainder === parseInt(digits[10]);
}

function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

type FormFields = {
  name: string;
  cpf: string;
  birthDate: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: string;
};

type FormErrors = Partial<Record<keyof FormFields, string>>;

function validate(form: FormFields): FormErrors {
  const errors: FormErrors = {};

  if (!form.name.trim()) errors.name = "Nome é obrigatório.";

  if (!form.cpf.trim()) {
    errors.cpf = "CPF é obrigatório.";
  } else if (!isValidCPF(form.cpf)) {
    errors.cpf = "CPF inválido.";
  }

  if (!form.birthDate) errors.birthDate = "Data de nascimento é obrigatória.";

  if (!form.email.trim()) {
    errors.email = "E-mail é obrigatório.";
  } else if (!isValidEmail(form.email)) {
    errors.email = "Formato de e-mail inválido.";
  }

  if (!form.role) errors.role = "Selecione um perfil.";

  if (!form.password) {
    errors.password = "Senha é obrigatória.";
  } else if (form.password.length < 6) {
    errors.password = "A senha deve ter no mínimo 6 caracteres.";
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = "Confirmação de senha é obrigatória.";
  } else if (form.confirmPassword !== form.password) {
    errors.confirmPassword = "As senhas não coincidem.";
  }

  return errors;
}

export function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<FormFields>({
    name: "",
    cpf: "",
    birthDate: "",
    email: "",
    password: "",
    confirmPassword: "",
    role: "",
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);

  function handleChange(
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) {
    const { name, value } = e.target;
    const updated = {
      ...form,
      [name]: name === "cpf" ? maskCPF(value) : value,
    };
    setForm(updated);
    if (submitted) setErrors(validate(updated));
  }

  function handleSubmit(e: React.SyntheticEvent) {
    e.preventDefault();
    setSubmitted(true);
    const validationErrors = validate(form);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    // TODO: integrar com backend
  }

  const inputClass = (field: keyof FormFields) =>
    `w-full border rounded-lg px-4 py-2.5 text-sm outline-none focus:ring-2 transition ${
      errors[field]
        ? "border-red-400 focus:ring-red-300 focus:border-red-400"
        : "border-gray-300 focus:ring-purple-400 focus:border-purple-400"
    }`;

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center px-4 py-10">
      <div className="bg-white rounded-2xl shadow-md w-full max-w-sm px-8 py-10">
        {/* Logo */}
        <div className="flex flex-col items-center mb-7">
          <svg
            className="w-10 h-10 text-purple-600 mb-2"
            fill="none"
            stroke="currentColor"
            strokeWidth={1.8}
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M4.318 6.318a4.5 4.5 0 016.364 0L12 7.636l1.318-1.318a4.5 4.5 0 116.364 6.364L12 20.364l-7.682-7.682a4.5 4.5 0 010-6.364z"
            />
          </svg>
          <h1 className="text-xl font-bold text-gray-800">Criar Conta</h1>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nome completo
            </label>
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              className={inputClass("name")}
            />
            {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              CPF
            </label>
            <input
              type="text"
              name="cpf"
              value={form.cpf}
              onChange={handleChange}
              placeholder="000.000.000-00"
              className={inputClass("cpf")}
            />
            {errors.cpf && <p className="text-red-500 text-xs mt-1">{errors.cpf}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Data de nascimento
            </label>
            <input
              type="date"
              name="birthDate"
              value={form.birthDate}
              onChange={handleChange}
              className={inputClass("birthDate")}
            />
            {errors.birthDate && <p className="text-red-500 text-xs mt-1">{errors.birthDate}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="seu@email.com"
              className={inputClass("email")}
            />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Perfil
            </label>
            <select
              name="role"
              value={form.role}
              onChange={handleChange}
              className={`${inputClass("role")} bg-white text-gray-700`}
            >
              <option value="" disabled>Selecione o perfil</option>
              <option value="paciente">Paciente</option>
              <option value="medico">Médico</option>
            </select>
            {errors.role && <p className="text-red-500 text-xs mt-1">{errors.role}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Senha
            </label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Mínimo 6 caracteres"
              className={inputClass("password")}
            />
            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Confirmar senha
            </label>
            <input
              type="password"
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              className={inputClass("confirmPassword")}
            />
            {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>}
          </div>

          <button
            type="submit"
            className="w-full bg-purple-600 hover:bg-purple-700 text-white font-semibold py-3 rounded-lg transition text-sm mt-2"
          >
            Criar conta
          </button>
        </form>

        {/* Link para login */}
        <div className="flex justify-center mt-5">
          <button
            type="button"
            onClick={() => navigate("/login")}
            className="text-sm text-purple-600 hover:text-purple-800 font-medium transition"
          >
            Já tem conta? Entrar
          </button>
        </div>
      </div>
    </div>
  );
}
