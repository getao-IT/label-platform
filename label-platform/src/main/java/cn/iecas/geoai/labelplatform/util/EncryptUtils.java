package cn.iecas.geoai.labelplatform.util;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import java.util.Base64;
import java.util.List;



/**
 * @author Hadoop
 */
public class EncryptUtils {


    public static String base64Decrypt(String value , int offset) {
        StringBuilder result = new StringBuilder();
        for (int index = 0 ; index < value.length(); index++) {
            result.append((char) (value.charAt(index) - offset));
        }
        result = new StringBuilder(new String(Base64.getDecoder().decode(result.toString())).trim());
        return result.toString();
    }


    public static List<Integer> decryptIdAndInt(List<Object> ids) {
        List<Integer> idList = Lists.newArrayList();
        for (Object id : ids) {
            if (StringUtils.isBlank((CharSequence) id)) {
                continue;
            }
            if (StringUtils.isNumeric(id.toString())) {
                idList.add(Integer.valueOf(id.toString()));
            }else {
                idList.add(Integer.valueOf(base64Decrypt(id.toString() , 3)));
            }
        }
        return idList;
    }
}
