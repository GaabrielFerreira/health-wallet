import { useNavigate } from "react-router-dom";

const features = [
  {
    icon: (
      <svg className="w-7 h-7" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    ),
    title: "Prontuário Digital",
    description: "Tenha seu histórico médico completo sempre acessível — alergias, doenças crônicas, medicamentos e muito mais, em um único lugar seguro.",
  },
  {
    icon: (
      <svg className="w-7 h-7" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
      </svg>
    ),
    title: "Histórico de Vacinação",
    description: "Registre cada dose aplicada com fabricante, lote e data. Nunca mais perca o controle da sua caderneta de vacinação.",
  },
  {
    icon: (
      <svg className="w-7 h-7" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
    title: "Consultas Clínicas",
    description: "Acompanhe seus atendimentos com data, especialidade, profissional e prescrições. Seu histórico de consultas organizado e acessível.",
  },
  {
    icon: (
      <svg className="w-7 h-7" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
      </svg>
    ),
    title: "Compartilhamento Seguro",
    description: "Conceda acesso ao seu médico via token temporário. Você controla quem vê seus dados e por quanto tempo — sem abrir mão da sua privacidade.",
  },
];

const steps = [
  { number: "01", title: "Crie sua conta", description: "Cadastre-se em segundos como paciente ou médico." },
  { number: "02", title: "Preencha seu perfil", description: "Adicione seus dados de saúde, vacinas e histórico de consultas." },
  { number: "03", title: "Acesse onde quiser", description: "Sua carteira de saúde digital disponível a qualquer hora." },
];

export function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-white text-gray-800 font-sans">

      {/* Navbar */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <svg className="w-7 h-7 text-purple-600" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M4.318 6.318a4.5 4.5 0 016.364 0L12 7.636l1.318-1.318a4.5 4.5 0 116.364 6.364L12 20.364l-7.682-7.682a4.5 4.5 0 010-6.364z" />
            </svg>
            <span className="font-bold text-gray-900 text-lg">Health Wallet</span>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate("/login")}
              className="text-sm font-medium text-gray-600 hover:text-purple-600 transition"
            >
              Entrar
            </button>
            <button
              onClick={() => navigate("/cadastro")}
              className="text-sm font-semibold bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg transition"
            >
              Criar conta
            </button>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="pt-32 pb-24 px-6 bg-gradient-to-br from-purple-50 via-white to-white">
        <div className="max-w-4xl mx-auto text-center">
          <span className="inline-block bg-purple-100 text-purple-700 text-xs font-semibold px-3 py-1 rounded-full mb-6 tracking-wide uppercase">
            Sua saúde, organizada
          </span>
          <h1 className="text-5xl sm:text-6xl font-extrabold text-gray-900 leading-tight mb-6">
            A carteira de saúde{" "}
            <span className="text-purple-600">digital</span> que você sempre precisou
          </h1>
          <p className="text-xl text-gray-500 max-w-2xl mx-auto mb-10 leading-relaxed">
            Tenha todo o seu histórico médico centralizado, seguro e acessível a qualquer hora — prontuário, vacinas, consultas e muito mais.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button
              onClick={() => navigate("/cadastro")}
              className="bg-purple-600 hover:bg-purple-700 text-white font-semibold px-8 py-4 rounded-xl text-base transition shadow-lg shadow-purple-200"
            >
              Começar gratuitamente
            </button>
            <button
              onClick={() => navigate("/login")}
              className="border border-gray-200 hover:border-purple-300 text-gray-700 font-semibold px-8 py-4 rounded-xl text-base transition"
            >
              Já tenho conta
            </button>
          </div>
          <p className="mt-8 text-sm text-gray-400">
            Ajude-nos a melhorar -{" "}
            <a
              href="https://docs.google.com/forms/d/e/1FAIpQLSe4c5igMFWXZVjVCzfquk_HJuOoi-hAqkh-TKdIq1iYB2gtMg/viewform"
              target="_blank"
              rel="noreferrer"
              className="text-purple-500 hover:text-purple-700 underline underline-offset-2 transition"
            >
              responda nossa pesquisa de validação
            </a>
          </p>
        </div>
      </section>

      {/* Features */}
      <section className="py-24 px-6 bg-white">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
              Tudo que você precisa para cuidar da sua saúde
            </h2>
            <p className="text-gray-500 text-lg max-w-xl mx-auto">
              Funcionalidades pensadas para pacientes e profissionais de saúde.
            </p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((f) => (
              <div key={f.title} className="group p-6 rounded-2xl border border-gray-100 hover:border-purple-200 hover:shadow-md transition">
                <div className="w-12 h-12 bg-purple-50 group-hover:bg-purple-100 rounded-xl flex items-center justify-center text-purple-600 mb-5 transition">
                  {f.icon}
                </div>
                <h3 className="font-semibold text-gray-900 mb-2">{f.title}</h3>
                <p className="text-sm text-gray-500 leading-relaxed">{f.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How it works */}
      <section className="py-24 px-6 bg-gray-50">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">Como funciona</h2>
            <p className="text-gray-500 text-lg">Simples, rápido e seguro.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-10">
            {steps.map((step) => (
              <div key={step.number} className="flex flex-col items-center text-center">
                <span className="text-5xl font-black text-purple-100 mb-4 leading-none">{step.number}</span>
                <h3 className="font-semibold text-gray-900 mb-2 text-lg">{step.title}</h3>
                <p className="text-sm text-gray-500 leading-relaxed">{step.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* For who */}
      <section className="py-24 px-6 bg-white">
        <div className="max-w-5xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">Para quem é o Health Wallet?</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-8">
            <div className="p-8 rounded-2xl bg-purple-50 border border-purple-100">
              <div className="w-12 h-12 bg-purple-600 rounded-xl flex items-center justify-center mb-5">
                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">Pacientes</h3>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex gap-2"><span className="text-purple-500 font-bold">✓</span> Centralize toda sua saúde em um só lugar</li>
                <li className="flex gap-2"><span className="text-purple-500 font-bold">✓</span> Acesse seu histórico de qualquer dispositivo</li>
                <li className="flex gap-2"><span className="text-purple-500 font-bold">✓</span> Compartilhe dados com seu médico com segurança</li>
                <li className="flex gap-2"><span className="text-purple-500 font-bold">✓</span> Mantenha controle total sobre sua privacidade</li>
              </ul>
            </div>
            <div className="p-8 rounded-2xl bg-gray-50 border border-gray-100">
              <div className="w-12 h-12 bg-gray-800 rounded-xl flex items-center justify-center mb-5">
                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">Médicos</h3>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex gap-2"><span className="text-gray-500 font-bold">✓</span> Acesse o histórico do paciente com autorização</li>
                <li className="flex gap-2"><span className="text-gray-500 font-bold">✓</span> Visualize anamnese, vacinas e consultas anteriores</li>
                <li className="flex gap-2"><span className="text-gray-500 font-bold">✓</span> Tome decisões com informações completas</li>
                <li className="flex gap-2"><span className="text-gray-500 font-bold">✓</span> Acesso seguro via token temporário do paciente</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* CTA final */}
      <section className="py-24 px-6 bg-purple-600">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-3xl sm:text-4xl font-bold text-white mb-4">
            Comece a cuidar da sua saúde hoje
          </h2>
          <p className="text-purple-200 text-lg mb-10">
            Crie sua conta gratuita e tenha seu prontuário digital em minutos.
          </p>
          <button
            onClick={() => navigate("/cadastro")}
            className="bg-white hover:bg-purple-50 text-purple-700 font-semibold px-10 py-4 rounded-xl text-base transition shadow-lg"
          >
            Criar conta gratuitamente
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-8 px-6 bg-white border-t border-gray-100">
        <div className="max-w-6xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" strokeWidth={1.8} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M4.318 6.318a4.5 4.5 0 016.364 0L12 7.636l1.318-1.318a4.5 4.5 0 116.364 6.364L12 20.364l-7.682-7.682a4.5 4.5 0 010-6.364z" />
            </svg>
            <span className="text-sm font-semibold text-gray-700">Health Wallet</span>
          </div>
          <p className="text-xs text-gray-400">© 2026 Health Wallet. UniFacens - Eng. Computação 7° Sem. 2026S1 · Disciplina Eng. Software</p>
        </div>
      </footer>

    </div>
  );
}
