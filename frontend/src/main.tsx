import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import DemoGate from "./DemoGate";
import "./styles.css";
import "./refinement.css";
ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <DemoGate><App /></DemoGate>
  </React.StrictMode>,
);
