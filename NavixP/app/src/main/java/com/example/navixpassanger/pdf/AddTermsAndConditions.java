package com.example.navixpassanger.pdf;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.util.ArrayList;
import java.util.List;

public class AddTermsAndConditions {
    static void addTermsAndConditions(Document document) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Terms and conditions")
                .setBold()
                .setFontSize(14));

        List<String> terms = new ArrayList<>();
        terms.add("1. Navix is an online ticketing platform. It does not operate bus services of its own. In order to provide a comprehensive choice of bus operators, departure times and prices to customers, it has tied up with many bus operators.\n");

        terms.add("Navix responsibilities include:\n" +
                "(1) Issuing a valid ticket (a ticket that will be accepted by the bus operator) for its network of bus operators\n" +
                "(2) Providing refund and support in the event of cancellation\n" +
                "(3) Providing customer support and information in case of any delays / inconvenience\n");

        terms.add("Navix responsibilities do not include:\n" +
                "(1) The bus operator's bus not departing / reaching on time.\n" +
                "(2) The bus operator's employees being rude.\n" +
                "(3) The bus operator's bus seats etc not being up to the customer's expectation.\n" +
                "(4) The bus operator canceling the trip due to unavoidable reasons.\n" +
                "(5) The baggage of the customer getting lost / stolen / damaged.\n" +
                "(6) The bus operator changing a customer's seat at the last minute to accommodate a lady / child.\n" +
                "(7) The customer waiting at the wrong boarding point (please call the bus operator to find out the exact boarding point if you are not a regular traveler on that particular bus).\n" +
                "(8) The bus operator changing the boarding point and/or using a pick-up vehicle at the boarding point to take customers to the bus departure point.\n");

        terms.add("2. The departure time mentioned on the ticket are only tentative timings. However the bus will not leave the source before the time that is mentioned on the ticket.\n");

        terms.add("3. Passengers are required to furnish the following at the time of boarding the bus:\n" +
                "(1) A digital copy of the e-ticket or m-ticket.\n" +
                "(2) A valid identity proof\n" +
                "Failing to do so, they may not be allowed to board the bus.\n");

        terms.add("4. Change of bus: In case the bus operator changes the type of bus due to some reason, Navix will refund the differential amount to the customer upon being intimated by the customers in 24 hours of the journey.\n");

        terms.add("5. Amenities for this bus as shown on Navix have been configured and provided by the bus provider (bus operator). These amenities will be provided unless there are some exceptions on certain days. Please note that Navix provides this information in good faith to help passengers to make an informed decision. The liability of the amenity not being made available lies with the operator and not with Navix.\n");

        terms.add("6. In case a booking confirmation e-mail and sms gets delayed or fails because of technical reasons or as a result of incorrect e-mail ID / phone number provided by the user etc, a ticket will be considered 'booked' as long as the ticket shows up on the confirmation page of www.navix.in\n");

        terms.add("7. Grievances and claims related to the bus journey should to be reported to Navix support team within 7 days of your Travel date.\n");

        terms.add("8. Cancellation of this ticket is NOT allowed after bus departure time.\n");

        for (String term : terms) {
            document.add(new Paragraph(term)
                    .setFontSize(10)
                    .setMarginLeft(20f));
        }
    }
}
