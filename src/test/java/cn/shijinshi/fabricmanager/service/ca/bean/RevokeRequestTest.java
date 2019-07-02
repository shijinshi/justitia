//package cn.shijinshi.service.fabricca.bean;
//
//import cn.shijinshi.service.ca.requester.entity.RevokeInfo;
//import org.hyperledger.fabric.sdk.Enrollment;
//import org.hyperledger.fabric.sdk.User;
//import org.junit.Test;
//
//public class RevokeRequestTest {
//
//
//    @Test
//    public void test(){
//        RevokeInfo.Reason reason = RevokeInfo.Reason.AFFILIATION_CHANGE;
//        User revoker = null;
//        boolean genCRL = true;
//        Enrollment enrollment = null;
//        RevokeInfo revokeRequest = new RevokeInfo(revoker,reason,genCRL,enrollment);
//        String reason1 = revokeRequest.getReason();
//        System.out.println(reason1);
//    }
//}