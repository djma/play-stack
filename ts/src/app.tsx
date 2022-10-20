import * as React from "react";
import { createRoot } from "react-dom/client";

import Main from "./view/Main";

const root = document.querySelector("#root")!;
createRoot(root).render(<App />);

function App() {
  return <Main />;
}
