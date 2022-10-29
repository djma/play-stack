package djma;

import static djma.common.Common.getBodyJson;
import static djma.common.Common.simpleObjectMapper;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

import java.io.IOException;
import java.util.Map;

import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import djma.common.Env;
import djma.db.DB;
import djma.db.generated.tables.records.ContactRecord;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GraphQLServlet extends HttpServlet {
    GraphQL graphQL;
    private final DB db = DB.get();
    private final Env env = Env.get();

    public GraphQLServlet() throws IOException {
        super();
        String schema = new String(GraphQLServlet.class.getResourceAsStream("schema.gql").readAllBytes());

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        StaticDataFetcher testTableDataFetcher = new StaticDataFetcher(
                db.run(ctx -> ctx.selectFrom("contact").limit(2).fetchInto(ContactRecord.class)));

        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("hello", new StaticDataFetcher("world"))
                        .dataFetcher("contacts", testTableDataFetcher))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private void setAccessControl(HttpServletResponse resp) {
        if (env.isProd()) {
            resp.setHeader("Access-Control-Allow-Origin", "https://play-stack.vercel.app");
            resp.setHeader("Access-Control-Request-Headers", "https://play-stack.vercel.app");
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Request-Headers", "*");
        }
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setAccessControl(resp);
        resp.setStatus(200);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setAccessControl(resp);
        doGql(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws JsonProcessingException, JsonMappingException, IOException {
        setAccessControl(resp);
        doGql(req, resp);
    }

    @SuppressWarnings("unchecked")
    private void doGql(HttpServletRequest req, HttpServletResponse resp)
            throws JsonProcessingException, JsonMappingException, IOException {
        Map<String, Object> gqlRequest = getBodyJson(req);
        String query = gqlRequest.get("query").toString();
        String operationName = gqlRequest.get("operationName").toString();

        Map<String, Object> variables = (Map<String, Object>) gqlRequest.get("variables");

        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables).build();

        ExecutionResult executionResult = this.graphQL.execute(input);

        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        byte[] respBytes = simpleObjectMapper.writeValueAsBytes(executionResult.toSpecification());
        resp.getOutputStream().write(respBytes);
    }
}