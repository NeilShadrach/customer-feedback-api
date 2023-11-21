package uk.gov.companieshouse.customerfeedbackapi.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.customerfeedbackapi.exception.*;
import uk.gov.companieshouse.customerfeedbackapi.mapper.CustomerFeedbackMapper;
import uk.gov.companieshouse.customerfeedbackapi.model.dao.CustomerFeedbackDAO;
import uk.gov.companieshouse.customerfeedbackapi.model.dao.CustomerFeedbackDataDAO;
import uk.gov.companieshouse.customerfeedbackapi.model.dto.CustomerFeedbackDTO;
import uk.gov.companieshouse.customerfeedbackapi.repository.CustomerFeedbackRepository;
import uk.gov.companieshouse.customerfeedbackapi.service.CustomerFeedbackService;
import uk.gov.companieshouse.customerfeedbackapi.utils.Helper;

@ExtendWith(MockitoExtension.class)
class CustomerFeedbackServiceTest {

  Helper helper = new Helper();

  private static final String REQUEST_ID = UUID.randomUUID().toString();
  private static final String EMAIL = "Test@Test.com";
  private static final String FEEDBACK = "Something went wrong";
  private static final String NAME = "A User";
  private static final String KIND = "feedback";
  private static final String SOURCE_URL = "http://chs.local";
  private static final LocalDateTime CREATED_AT = LocalDateTime.now();
  private static final boolean SENT_EMAIL = true;
  private CustomerFeedbackDTO customerFeedbackDTO =
      helper.generateCustomerFeedbackDTO(EMAIL, FEEDBACK, NAME, KIND, SOURCE_URL);
  private CustomerFeedbackDAO customerFeedbackDAO =
      helper.generateCustomerFeedbackDAO(
          EMAIL, FEEDBACK, NAME, KIND, SOURCE_URL, CREATED_AT, SENT_EMAIL);

  @Mock private CustomerFeedbackMapper customerFeedbackMapper;

  @Mock private CustomerFeedbackRepository customerFeedbackRepository;

  @Mock private URL url;

  @InjectMocks private CustomerFeedbackService customerFeedbackService;

  @Test
  void testCreateCustomerFeedbackIsSuccessful() throws SendEmailException {

    when(customerFeedbackMapper.dtoToDao(any())).thenReturn(customerFeedbackDAO);
    when(customerFeedbackRepository.insert(customerFeedbackDAO)).thenReturn(customerFeedbackDAO);

    ReflectionTestUtils.setField(customerFeedbackService, "emailSendFlag", true);
    ReflectionTestUtils.setField(customerFeedbackService, "kafkaApiEndpoint", "http://localhost");
    customerFeedbackService.createCustomerFeedback(customerFeedbackDTO, REQUEST_ID);

    verify(customerFeedbackMapper, times(1)).dtoToDao(any());
  }

  @Test
  void testCreateCustomerFeedbackFailsSendEmailException() throws SendEmailException, IOException {
    when(customerFeedbackMapper.dtoToDao(any())).thenReturn(customerFeedbackDAO);
    when(customerFeedbackRepository.insert(customerFeedbackDAO)).thenReturn(customerFeedbackDAO);

    ReflectionTestUtils.setField(customerFeedbackService, "emailSendFlag", true);
    ReflectionTestUtils.setField(
        customerFeedbackService, "kafkaApiEndpoint", "InvalidURL"); // Causes exception to be thrown
    try {
      customerFeedbackService.createCustomerFeedback(customerFeedbackDTO, REQUEST_ID);
      fail("No exception thrown");
    } catch (Exception e) {
      assertEquals(SendEmailException.class, e.getClass());
    }
  }

}
