import * as React from "react";
import { createRoot } from "react-dom/client";
import { ApolloClient, ApolloProvider, InMemoryCache } from "@apollo/client";

import Main from "./view/Main";

const root = document.querySelector("#root")!;
createRoot(root).render(<App />);

const client = new ApolloClient({
  uri: "https://play-stack.herokuapp.com/gql",
  // uri: "http://localho.st:8080/gql",
  cache: new InMemoryCache(),
});

function App() {
  return (
    <ApolloProvider client={client}>
      <Main />
    </ApolloProvider>
  );
}
