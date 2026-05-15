import { Toaster } from "react-hot-toast";
import { AppRoutes } from "./routes";

function App() {
  return (
    <>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            fontSize: "14px",
            borderRadius: "8px",
            padding: "12px 16px",
          },
          success: { iconTheme: { primary: "#9333ea", secondary: "#fff" } },
        }}
      />
      <AppRoutes />
    </>
  );
}

export default App;
