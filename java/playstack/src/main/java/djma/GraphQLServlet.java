package djma;

import static djma.common.Common.simpleObjectMapper;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.ContentType;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getParameter("query");
        String operationName = req.getParameter("operationName");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = req.getParameter("variables") != null
                ? simpleObjectMapper.readValue(req.getParameter("variables"), HashMap.class)
                : new HashMap<String, Object>();

        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables).build();

        ExecutionResult executionResult = this.graphQL.execute(input);

        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        byte[] respBytes = simpleObjectMapper.writeValueAsBytes(executionResult.toSpecification());
        resp.getOutputStream().write(respBytes);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

    }
}