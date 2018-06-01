#!/usr/bin/env python
# encoding: utf-8
import json
from functools import wraps

import LocationContext
from Model.AuthorizationModel import AuthorizationModel
from Model.WebUILogModel import WebUILogModel
from SessionManager import SessionManager
from Utility.EncryptUtil import EncryptUtil
from Utility.InteractionUtil import InteractionUtil
from Utility.LogUtil import LogUtil
import GlobalConfigContext as GCC


def authorizeRequireWarp(fn):
    """
    Decorator for session valid required.
    """
    @wraps(fn)
    def wrapper(self, session, *args, **kwargs):
        try:
            if SessionManager.Check(session) is True:
                return fn(self, session, *args, **kwargs)
            else:
                return False, RenUIController.Unauthorized(session)
        except Exception as e:
            print "Exception in COrgan: %s" % str(e)
            return False, e
    return wrapper


def adminRequireWarp(fn):
    """
    Decorator for session admin valid required.
    """
    @wraps(fn)
    def wrapper(self, session, *args, **kwargs):
        try:
            if SessionManager.CheckAdmin(session) is True:
                return fn(self, session, *args, **kwargs)
            else:
                return False, RenUIController.Unauthorized(session)
        except Exception as e:
            print "Exception in COrgan: %s" % str(e)
            return False, e
    return wrapper


def ExceptionWarp(fn):
    """
    Decorator for COrgan std exception.
    """
    @wraps(fn)
    def wrapper(*args, **kwargs):
        try:
            return fn(*args, **kwargs)
        except Exception as e:
            print "Exception in COrgan: %s" % str(e)
            return False, e
    return wrapper


class RenUIController:
    """
    Ren Web UI Controller.
    All requests will be handled here without difference of view.
    """

    def __init__(self):
        pass

    @staticmethod
    def Auth(username, rawPassword):
        """
        Get authorization token by username and password
        :param username: unique username string, in pattern of username@domain
        :param rawPassword: password without encryption
        :return:
        """
        try:
            retVal = SessionManager.Login(username, EncryptUtil.EncryptSHA256(rawPassword))
            return retVal is not None, retVal
        except:
            return None, False

    @staticmethod
    def Disconnect(token):
        """
        Destroy a auth token
        :param token: auth token string
        :return:
        """
        return True, SessionManager.Logout(token)

    @staticmethod
    def AmIAdmin(session):
        """
        Get whether I am an admin.
        :param session: session id
        :return: True if admin session
        """
        try:
            return True, SessionManager.CheckAdmin(session)
        except Exception as e:
            print "Exception in WebUI: %s" % str(e)
            return False, e

    @staticmethod
    def Unauthorized(session):
        """
        Warp unauthorized service request feedback package.
        :param session: session id
        :return: unauthorized feedback
        """
        try:
            sObj = SessionManager.GetSession(session)
            sUser = ""
            if sObj is not None:
                sUser = sObj.Username
            LogUtil.Log("username:%s, session:%s unauthorized request." % (sUser, session),
                        RenUIController.__name__, "Warning", True)
        except Exception as e:
            print "Exception in RenWebUI authorization check: %s" % str(e)
        finally:
            return GCC.UNAUTHORIZED

    """
    Domain Management Methods
    """
    @adminRequireWarp
    @ExceptionWarp
    def DomainAdd(self, session, name, raw_password, corgan):
        """
        Add a domain.
        :param session: session id
        :param name: domain name
        :param raw_password: domain password
        :param corgan: domain binding COrgan location
        """
        pd = {"name": name, "password": raw_password, "corgan": corgan}
        return True, InteractionUtil.Send(LocationContext.URL_Domain_Add, pd)

    @adminRequireWarp
    @ExceptionWarp
    def DomainStop(self, session, name):
        """
        Ban a domain.
        :param session: session id
        :param name: domain's name to be stopped
        """
        pd = {"name": name, "status": 1}
        dt = InteractionUtil.Send(LocationContext.URL_Domain_Update, pd)
        return True, json.loads(dt["data"], encoding="utf8")

    @adminRequireWarp
    @ExceptionWarp
    def DomainResume(self, session, name):
        """
        Resume a domain.
        :param session: session id
        :param name: domain's name to be resumed
        """
        pd = {"name": name, "status": 0}
        dt = InteractionUtil.Send(LocationContext.URL_Domain_Update, pd)
        return True, json.loads(dt["data"], encoding="utf8")

    @authorizeRequireWarp
    @ExceptionWarp
    def DomainUpdate(self, session, name, new_corgan):
        """
        Update a domain info.
        :param session: session id
        :param name: domain's name to be updated
        :param new_corgan: domain new COrgan location
        """
        pd = {"name": name, "corgan": new_corgan}
        dt = InteractionUtil.Send(LocationContext.URL_Domain_Update, pd)
        return True, json.loads(dt["data"], encoding="utf8")

    @authorizeRequireWarp
    @ExceptionWarp
    def DomainGet(self, session, name):
        """
        Get a domain.
        :param session: session id
        :param name: domain to be retrieve
        """
        pd = {"name": name}
        dt = InteractionUtil.Send(LocationContext.URL_Domain_Get, pd)
        return True, json.loads(dt["data"], encoding="utf8")

    @adminRequireWarp
    @ExceptionWarp
    def DomainGetAll(self, session):
        """
        Get all domain as a list.
        :param session: session id
        """
        dt = InteractionUtil.Send(LocationContext.URL_Domain_GetAll)
        return True, json.loads(dt["data"], encoding="utf8")

    AuthorizationModel.Initialize(forced=True)
    WebUILogModel.Initialize(forced=True)


RenUIControllerInstance = RenUIController()