package org.tools.web.dbops;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tools.web.model.ThreadDetail;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DbUtils {

  private HibernateUtil hibernateUtil;

  @Autowired
  public DbUtils(HibernateUtil hibernateUtil) {
    this.hibernateUtil = hibernateUtil;
  }

  public void addThreadDetailsToDb(List<ThreadDetail> threadDetails) {
    Transaction transaction = null;
    try (Session session = hibernateUtil.getSessionFactory().openSession()) {

      transaction = session.beginTransaction();
      threadDetails.forEach(threadDetail -> {
        session.save(threadDetail);
      });
      transaction.commit();
    }
  }

  public void importFile(List<ThreadDetail> threadDetailList, String fileName) {
//    List<ThreadDetail> existingThreadsDumps = loadExistingThreads(fileName);
    addThreadDetailsToDb(threadDetailList);
  }

  public List<ThreadDetail> loadExistingThreads(String fileName) {
    try (Session session = hibernateUtil.getSessionFactory().openSession()) {
      Query q=session.createQuery("From ThreadDetail where fileIdentifier=:n");
      q.setParameter("n", fileName);
      List<ThreadDetail> list = q.list();
      return list;
    }
  }

  public List<ThreadDetail> loadExistingThreadsByBatchNumber(String batchNumber) {
    try (Session session = hibernateUtil.getSessionFactory().openSession()) {
      Query q=session.createQuery("From ThreadDetail where batchNumber=:n");
      q.setFirstResult(0);
      q.setMaxResults(10);
      q.setParameter("n", batchNumber);
      List<ThreadDetail> list = q.list();
      return list;
    }
  }
}
