import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import DemoGate from "./DemoGate";
import PublicDemo from "./PublicDemo";
import "./styles.css";
import "./refinement.css";
const showPublicDemo = import.meta.env.MODE === "hosted" &&
  !new URLSearchParams(window.location.search).has("workspace");
ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    {showPublicDemo ? <PublicDemo /> : <DemoGate><App /></DemoGate>}
  </React.StrictMode>,
);
