import * as React from "react";
import { createRoot } from "react-dom/client";
import {
  ApolloClient,
  ApolloProvider,
  createHttpLink,
  InMemoryCache,
} from "@apollo/client";
import { ConnectButton } from "@rainbow-me/rainbowkit";
import { getDefaultWallets, RainbowKitProvider } from "@rainbow-me/rainbowkit";
import {
  chain,
  configureChains,
  createClient,
  WagmiConfig,
  useAccount,
  useNetwork,
  useSignMessage,
} from "wagmi";

import { publicProvider } from "wagmi/providers/public";

import Main from "./view/Main";

// const serverUrl = "http://127.0.0.1:8080";
// const serverUrl = "http://localho.st:8080";
const serverUrl = "https://play-stack.herokuapp.com";

const root = document.querySelector("#root")!;
createRoot(root).render(<App />);

const client = new ApolloClient({
  credentials: "include",
  link: createHttpLink({
    uri: serverUrl + "/gql",
    credentials: "include",
  }),
  cache: new InMemoryCache(),
});

// Connect to Ethereum via wagmi
const { chains, provider } = configureChains(
  [chain.mainnet],
  // [],
  [publicProvider()]
);
const { connectors } = getDefaultWallets({ appName: "Hello Foundry", chains });
const wagmiClient = createClient({ autoConnect: true, connectors, provider });

function App() {
  return (
    <WagmiConfig client={wagmiClient}>
      <RainbowKitProvider chains={chains}>
        <Profile />
        <ApolloProvider client={client}>
          <Main />
        </ApolloProvider>
      </RainbowKitProvider>
    </WagmiConfig>
  );
}

function SignInButton({
  onSuccess,
  onError,
}: {
  onSuccess: (args: { address: string; authToken?: string }) => void;
  onError: (args: { error: Error }) => void;
}) {
  const [state, setState] = React.useState<{
    loading?: boolean;
    nonce?: string;
  }>({});

  const fetchNonce = async () => {
    try {
      const nonceRes = await fetch(serverUrl + "/auth/nonce", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ address }),
      });
      const nonce = await nonceRes.text();
      setState((x) => ({ ...x, nonce }));
    } catch (error) {
      setState((x) => ({ ...x, error: error as Error }));
    }
  };

  // Pre-fetch random nonce when button is rendered
  // to ensure deep linking works for WalletConnect
  // users on iOS when signing the SIWE message
  React.useEffect(() => {
    fetchNonce();
  }, []);

  const { address } = useAccount();
  const { chain: activeChain } = useNetwork();
  const { signMessageAsync } = useSignMessage();

  const signIn = async () => {
    try {
      const chainId = activeChain?.id;
      if (!address || !chainId) return;

      setState((x) => ({ ...x, loading: true }));
      const message = state.nonce || "";
      const signature = await signMessageAsync({
        message: message,
      });

      // Verify signature
      const verifyRes = await fetch(serverUrl + "/auth/verify", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ message, signature, address }),
      });
      if (!verifyRes.ok) throw new Error("Error verifying message");

      const jsonResp = await verifyRes.json();

      const authToken = jsonResp.authToken;
      console.log("setting authToken ", authToken);
      setState((x) => ({ ...x, loading: false }));
      onSuccess({ address, authToken: authToken });
    } catch (error) {
      setState((x) => ({ ...x, loading: false, nonce: undefined }));
      onError({ error: error as Error });
      fetchNonce();
    }
  };

  return (
    <button disabled={!state.nonce || state.loading} onClick={signIn}>
      Sign-In with Ethereum
    </button>
  );
}

export function Profile() {
  const { isConnected } = useAccount();

  const [state, setState] = React.useState<{
    address?: string;
    authToken?: string;
    error?: Error;
    loading?: boolean;
  }>({});

  // Fetch user when:
  React.useEffect(() => {
    const handler = async () => {
      try {
        console.log("authToken ", state.authToken);
        const res = await fetch(serverUrl + "/auth/me", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ authToken: state.authToken }),
        });
        const json = await res.json();
        setState((x) => ({ ...x, address: json.address }));
      } catch (_error) {}
    };
    // 1. page loads
    handler();

    // 2. window is focused (in case user logs out of another window)
    window.addEventListener("focus", handler);
    return () => window.removeEventListener("focus", handler);
  }, [state.authToken]);

  if (isConnected) {
    return (
      <div>
        {/* Account content goes here */}

        {state.address ? (
          <div>
            <div>Signed in as {state.address}</div>
            <button
              onClick={async () => {
                await fetch(serverUrl + "/auth/logout");
                setState({});
              }}
            >
              Sign Out
            </button>
          </div>
        ) : (
          <SignInButton
            onSuccess={({ address, authToken }) => {
              console.log("onSuccess", address, authToken);
              setState((x) => ({ ...x, address, authToken }));
            }}
            onError={({ error }) => setState((x) => ({ ...x, error }))}
          />
        )}
      </div>
    );
  }

  return (
    <div>
      <ConnectButton />
    </div>
  );
}
