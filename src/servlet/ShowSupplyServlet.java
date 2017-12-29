package servlet;

import com.alibaba.fastjson.JSON;
import com.sun.rowset.CachedRowSetImpl;
import model.Demand;
import model.DemandsJson;
import model.SuppliesJson;
import model.Supply;
import util.DateUtil;
import util.DbConn;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ShowSupplyServlet" ,urlPatterns = {"/servlet/showSupply"})
public class ShowSupplyServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setHeader("content-type","text/html;charset=UTF-8");
        //连接数据库，查询需求列表

        int id = -1;
        if (request.getParameter("partId") != null) {
            id = Integer.parseInt(request.getParameter("partId"));
        }
        Connection connection = DbConn.getConnection();
        PreparedStatement pStatement = null;
        try {
            if (id == -1){//id为-1代表查询所有用户的需求
                pStatement = connection.prepareStatement("select partId,partPrice,partNum,created,supplierId,id from supply");
            }else{//否则查询某一具体用户的需求
                pStatement = connection.prepareStatement("select partId,partPrice,partNum,created,supplierId,id from supply WHERE partId=?");
                pStatement.setInt(1,id);
            }
            ResultSet resultSet = pStatement.executeQuery();
            CachedRowSetImpl rowSet = null;
            rowSet = new CachedRowSetImpl();
            rowSet.populate(resultSet);
            //rowSet.last();



            SuppliesJson suppliesJson = new SuppliesJson();
            List<Supply> supplyList = new ArrayList<>();

            //写入数据
            while (rowSet.next()){
                Supply temp = new Supply();
                temp.setPartId(rowSet.getInt(1));
                temp.setPartPrice(rowSet.getDouble(2));
                temp.setPartNum(rowSet.getInt(3));
                temp.setCreated(DateUtil.SQLDatetimeToString(rowSet.getDate(4)));

                pStatement = connection.prepareStatement("select name from parts WHERE id = ?");
                pStatement.setInt(1,rowSet.getInt(1));
                ResultSet resultSet1 = pStatement.executeQuery();
                CachedRowSetImpl rowSet1 = null;
                rowSet1 = new CachedRowSetImpl();
                rowSet1.populate(resultSet1);
                rowSet1.last();
                temp.setPartName(rowSet1.getString(1));

                temp.setSupplierId(rowSet.getInt(5));

                pStatement = connection.prepareStatement("select name from supplier WHERE id = ?");
                pStatement.setInt(1,rowSet.getInt(5));
                ResultSet resultSet2 = pStatement.executeQuery();
                CachedRowSetImpl rowSet2 = null;
                rowSet2 = new CachedRowSetImpl();
                rowSet2.populate(resultSet2);
                rowSet2.last();
                temp.setSupplierName(rowSet2.getString(1));

                temp.setSupplyId(rowSet.getInt(6));
                supplyList.add(temp);

                resultSet1.close();
            }
            suppliesJson.setSupplyList(supplyList);
            suppliesJson.setSupplierId(id);



            //关闭数据库连接
            resultSet.close();
            connection.close();

            String jsonString = JSON.toJSONString(suppliesJson);
            OutputStream out = response.getOutputStream();
            out.write(jsonString.getBytes("UTF-8"));
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
