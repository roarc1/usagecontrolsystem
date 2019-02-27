package iit.cnr.it.peprest;

import static iit.cnr.it.peprest.PEPRestServiceScenarioTest.PEPRestOperation.END_ACCESS;
import static iit.cnr.it.peprest.PEPRestServiceScenarioTest.PEPRestOperation.ON_GOING_RESPONSE;
import static iit.cnr.it.ucsinterface.node.NodeInterface.ENDACCESS_REST;
import static iit.cnr.it.ucsinterface.node.NodeInterface.ONGOINGRESPONSE_REST;
import static iit.cnr.it.ucsinterface.node.NodeInterface.STARTACCESS_REST;
import static iit.cnr.it.ucsinterface.node.NodeInterface.TRYACCESS_REST;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.jgiven.annotation.ScenarioStage;
import com.tngtech.jgiven.junit.ScenarioTest;

import iit.cnr.it.peprest.jgiven.stages.GivenContextHandlerRestSimulator;
import iit.cnr.it.peprest.jgiven.stages.GivenMessage;
import iit.cnr.it.peprest.jgiven.stages.ThenMessage;
import iit.cnr.it.peprest.jgiven.stages.WhenPEPRestService;
import oasis.names.tc.xacml.core.schema.wd_17.DecisionType;

@RunWith(DataProviderRunner.class)
public class PEPRestServiceScenarioTest
	extends ScenarioTest<GivenContextHandlerRestSimulator, WhenPEPRestService, ThenMessage> {

	@ScenarioStage
	GivenMessage givenMessage;

	public enum PEPRestOperation{
		TRY_ACCESS( TRYACCESS_REST ),
		START_ACCESS( STARTACCESS_REST ),
		END_ACCESS( ENDACCESS_REST ),
		ON_GOING_RESPONSE( ONGOINGRESPONSE_REST );

		private String operationUri;

		PEPRestOperation(String operationUri){
			this.operationUri = operationUri;
		}
		public String getOperationUri() {
			return this.operationUri;
		}
	}

    @DataProvider
    public static Object[][] dataPepRestOperations() {
        return new Object[][] {
                { PEPRestOperation.TRY_ACCESS },
                { PEPRestOperation.START_ACCESS },
                { PEPRestOperation.END_ACCESS },
        };
    }

    @Test
    @UseDataProvider("dataPepRestOperations")
	public void an_access_message_can_be_delivered_to_UCS(PEPRestOperation restOperation){
	    given().a_test_configuration_for_request_with_policy()
	    	.with().a_test_session_id()
	    	.and().a_mocked_context_handler_for_$(restOperation.getOperationUri())
	    	.with().a_success_response_status_$(HttpStatus.SC_OK);

	    when().PEPRest_service_$_is_executed(restOperation);

	    then().the_$_message_is_put_in_the_unanswered_queue(restOperation)
	    	.and().the_message_id_in_the_unanswered_queue_matches_the_one_sent()
	    	.and().the_asynch_HTTP_POST_request_for_$_was_received_by_context_handler(restOperation.getOperationUri());
	}

	@Test
	@UseDataProvider("dataPepRestOperations")
	public void ignore_access_message_delivered_to_UCS_if_fault_response_is_received(PEPRestOperation restOperation){
	    given().a_test_configuration_for_request_with_policy()
	    	.with().a_test_session_id()
	    	.and().a_mocked_context_handler_for_$(restOperation.getOperationUri())
	    	.with().a_fault_response();

	    when().PEPRest_service_$_execution_fails(restOperation);

	    then().an_illegal_access_exception_is_thrown()
	    	.and().the_Message_is_not_placed_into_the_unanswered_queue()
	    	.but().the_asynch_HTTP_POST_request_for_$_was_received_by_context_handler(restOperation.getOperationUri());
	}

    @Test
	public void a_response_message_for_an_on_going_evalutaion_can_be_delivered_to_UCS(){
    	givenMessage.given().a_ReevaluationResponse_request_with_decision_$(DecisionType.PERMIT);
	    given().and().a_test_configuration_for_request_with_policy()
	    	.and().a_mocked_context_handler_for_$(END_ACCESS.getOperationUri())
	    	.with().a_success_response_status_$(HttpStatus.SC_OK);

	    when().PEPRest_service_$_is_executed(ON_GOING_RESPONSE);

	    then().the_$_message_is_put_in_the_unanswered_queue(END_ACCESS)
	    	.and().the_Message_motivation_is_OK()
	    	.and().the_message_id_in_the_unanswered_queue_matches_the_one_sent()
	    	.and().the_asynch_HTTP_POST_request_for_$_was_received_by_context_handler(END_ACCESS.getOperationUri());
	}
    
    @Test
	public void a_response_message_fails_to_be_sent_to_context_handler_is_ignored(){
    	givenMessage.given().a_ReevaluationResponse_request_with_decision_$(DecisionType.PERMIT);
	    given().and().a_test_configuration_for_request_with_policy()
	    	.and().a_mocked_context_handler_for_$(END_ACCESS.getOperationUri())
	    	.with().a_fault_response();

	    when().PEPRest_service_$_is_executed(ON_GOING_RESPONSE);

	    then().the_Message_motivation_is_NOT_OK()
	    	.and().the_Message_is_not_placed_into_the_unanswered_queue()
	    	.but().the_asynch_HTTP_POST_request_for_$_was_received_by_context_handler(END_ACCESS.getOperationUri());
	}
}
