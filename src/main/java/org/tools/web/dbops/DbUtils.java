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

  public void importFile(List<ThreadDetail> threadDetailList) {
    addThreadDetailsToDb(threadDetailList);
  }

  public List<ThreadDetail> loadExistingThreads(List<ThreadDetail> threadDetails) {
    try (Session session = hibernateUtil.getSessionFactory().openSession()) {

      CriteriaBuilder cb = session.getCriteriaBuilder();
      CriteriaQuery<ThreadDetail> cq = cb.createQuery(ThreadDetail.class);
      Root<ThreadDetail> root = cq.from(ThreadDetail.class);

      List<String> nameList = threadDetails.stream().map(ThreadDetail::getFileIdentifier).collect(Collectors.toList());
      cq.where(root.get("fileIdentifier").in(nameList)); // Use the in() method

      TypedQuery<ThreadDetail> query = session.createQuery(cq);
      List<ThreadDetail> actualThreads = query.getResultList();

      return actualThreads;
    }
  }
}
